import { useStore } from '@/stores/useStore'
import type { Anchor, GameMode } from '@/types'

/**
 * Game Engine for MCA
 * 
 * Handles game logic for different modes:
 * - sticker_dance: 贴纸舞模式
 * - attack_defense: 攻守擂(PK)模式
 * - free: 自由模式
 */

export interface GameState {
  mode: GameMode
  round: number
  score: Record<string, number>
  anchors: GameAnchor[]
  combo: number
  progress: number
  countdown?: number
  isRunning: boolean
  startTime?: number
}

export interface GameAnchor {
  id: string
  name: string
  avatar?: string
  score: number
  rank: number
  isActive: boolean
  exclusiveGifts: string[]
  giftCount: number
}

export interface GameEvent {
  eventType: 'GIFT_RECEIVED' | 'ANCHOR_SWITCH' | 'ROUND_END' | 'COUNTDOWN_TICK' | 'COMBO_TRIGGER'
  anchorId?: string
  giftId?: string
  giftName?: string
  userName?: string
  quantity?: number
  diamondCost?: number
  effect?: string
}

/**
 * Sticker Dance Mode Engine
 */
export class StickerDanceEngine {
  private state: GameState
  private config: {
    countdownEnabled: boolean
    countdownDuration: number
    decayEnabled: boolean
    decayDuration: number
    autoFlipEnabled: boolean
    flipInterval: number
    maxPages: number
  }

  constructor(anchors: Anchor[], config: any) {
    this.config = config
    this.state = {
      mode: 'sticker_dance',
      round: 1,
      score: {},
      anchors: anchors.map(a => ({
        id: a.id,
        name: a.name,
        avatar: a.avatar,
        score: 0,
        rank: 0,
        isActive: !a.isEliminated,
        exclusiveGifts: a.exclusiveGifts,
        giftCount: 0,
      })),
      combo: 0,
      progress: 0,
      countdown: config.countdownEnabled ? config.countdownDuration : undefined,
      isRunning: false,
    }
    
    // Initialize scores
    anchors.forEach(a => {
      this.state.score[a.id] = 0
    })
  }

  start(): GameState {
    this.state.isRunning = true
    this.state.startTime = Date.now()
    return this.state
  }

  pause(): GameState {
    this.state.isRunning = false
    return this.state
  }

  reset(): GameState {
    this.state.round = 1
    this.state.combo = 0
    this.state.progress = 0
    this.state.countdown = this.config.countdownEnabled ? this.config.countdownDuration : undefined
    this.state.anchors.forEach(a => {
      a.score = 0
      a.giftCount = 0
    })
    Object.keys(this.state.score).forEach(key => {
      this.state.score[key] = 0
    })
    this.updateRanks()
    return this.state
  }

  processGift(anchorId: string, giftValue: number, quantity: number = 1): { state: GameState; event: GameEvent } {
    const anchor = this.state.anchors.find(a => a.id === anchorId)
    if (!anchor || !anchor.isActive) {
      return { state: this.state, event: { eventType: 'GIFT_RECEIVED' } }
    }

    // Update score
    const points = giftValue * quantity
    anchor.score += points
    anchor.giftCount += quantity
    this.state.score[anchorId] = anchor.score

    // Update combo
    this.state.combo++

    // Update progress (for progress bar)
    const maxScore = Math.max(...this.state.anchors.map(a => a.score), 1)
    this.state.progress = anchor.score / maxScore

    // Update ranks
    this.updateRanks()

    // Reset countdown if enabled
    if (this.config.countdownEnabled) {
      this.state.countdown = this.config.countdownDuration
    }

    const event: GameEvent = {
      eventType: 'GIFT_RECEIVED',
      anchorId,
      quantity,
      effect: this.getGiftEffect(quantity),
    }

    return { state: this.state, event }
  }

  tickCountdown(): { state: GameState; event?: GameEvent } {
    if (!this.config.countdownEnabled || !this.state.isRunning) {
      return { state: this.state }
    }

    if (this.state.countdown && this.state.countdown > 0) {
      this.state.countdown--
    }

    // Handle decay
    if (this.config.decayEnabled && this.state.countdown === 0) {
      this.applyDecay()
    }

    const event: GameEvent = {
      eventType: 'COUNTDOWN_TICK',
    }

    return { state: this.state, event }
  }

  private applyDecay() {
    // Reset all scores when countdown reaches 0
    this.state.anchors.forEach(a => {
      a.score = 0
      a.giftCount = 0
    })
    Object.keys(this.state.score).forEach(key => {
      this.state.score[key] = 0
    })
    this.state.combo = 0
    this.state.progress = 0
    this.updateRanks()
  }

  private updateRanks() {
    // Sort by score descending and assign ranks
    const sorted = [...this.state.anchors]
      .filter(a => a.isActive)
      .sort((a, b) => b.score - a.score)
    
    sorted.forEach((anchor, index) => {
      anchor.rank = index + 1
    })
  }

  private getGiftEffect(quantity: number): string {
    if (quantity >= 100) return 'legendary'
    if (quantity >= 50) return 'epic'
    if (quantity >= 20) return 'rare'
    if (quantity >= 10) return 'uncommon'
    return 'common'
  }

  getState(): GameState {
    return this.state
  }
}

/**
 * Attack Defense (PK) Mode Engine
 */
export class AttackDefenseEngine {
  private state: GameState
  private config: {
    defenderAdvantage: boolean
    attackerAdvantage: boolean
    allowDraw: boolean
    scoringMethod: 'individual' | 'split' | 'defender'
    freezeEffectEnabled: boolean
    freezeThresholds: { low: number; medium: number; high: number }
  }
  private defenders: string[]
  private attackers: string[]

  constructor(defenders: Anchor[], attackers: Anchor[], config: any) {
    this.config = config
    this.defenders = defenders.map(a => a.id)
    this.attackers = attackers.map(a => a.id)
    
    const allAnchors = [...defenders, ...attackers]
    
    this.state = {
      mode: 'attack_defense',
      round: 1,
      score: {},
      anchors: allAnchors.map(a => ({
        id: a.id,
        name: a.name,
        avatar: a.avatar,
        score: 0,
        rank: 0,
        isActive: !a.isEliminated,
        exclusiveGifts: a.exclusiveGifts,
        giftCount: 0,
      })),
      combo: 0,
      progress: 0.5, // Start at 50%
      isRunning: false,
    }
    
    allAnchors.forEach(a => {
      this.state.score[a.id] = 0
    })
  }

  start(): GameState {
    this.state.isRunning = true
    this.state.startTime = Date.now()
    return this.state
  }

  pause(): GameState {
    this.state.isRunning = false
    return this.state
  }

  endRound(): { state: GameState; winner: 'defender' | 'attacker' | 'draw' } {
    const defenderScore = this.getTeamScore('defender')
    const attackerScore = this.getTeamScore('attacker')
    
    let winner: 'defender' | 'attacker' | 'draw'
    
    if (defenderScore > attackerScore) {
      winner = 'defender'
    } else if (attackerScore > defenderScore) {
      winner = 'attacker'
    } else {
      // Draw - apply advantage rules
      if (this.config.defenderAdvantage) {
        winner = 'defender'
      } else if (this.config.attackerAdvantage) {
        winner = 'attacker'
      } else if (this.config.allowDraw) {
        winner = 'draw'
      } else {
        winner = 'defender' // Default to defender
      }
    }
    
    this.state.round++
    
    // Reset scores for next round based on scoring method
    if (this.config.scoringMethod === 'defender' && winner === 'attacker') {
      // If defender wins all, keep defender scores
    } else {
      this.state.anchors.forEach(a => {
        a.score = 0
        a.giftCount = 0
      })
      Object.keys(this.state.score).forEach(key => {
        this.state.score[key] = 0
      })
    }
    
    this.state.progress = 0.5
    
    return { state: this.state, winner }
  }

  processGift(anchorId: string, giftValue: number, quantity: number = 1): { state: GameState; event: GameEvent } {
    const anchor = this.state.anchors.find(a => a.id === anchorId)
    if (!anchor || !anchor.isActive) {
      return { state: this.state, event: { eventType: 'GIFT_RECEIVED' } }
    }

    // Update score
    const points = giftValue * quantity
    anchor.score += points
    anchor.giftCount += quantity
    this.state.score[anchorId] = anchor.score

    // Update progress bar (0-1, 0.5 is balanced)
    const defenderTotal = this.getTeamScore('defender')
    const attackerTotal = this.getTeamScore('attacker')
    const total = defenderTotal + attackerTotal
    
    if (total > 0) {
      this.state.progress = defenderTotal / total
    }

    // Check freeze effect
    const freezeLevel = this.getFreezeLevel()

    const event: GameEvent = {
      eventType: 'GIFT_RECEIVED',
      anchorId,
      quantity,
      effect: freezeLevel > 0 ? `freeze_${freezeLevel}` : undefined,
    }

    return { state: this.state, event }
  }

  private getTeamScore(team: 'defender' | 'attacker'): number {
    const teamIds = team === 'defender' ? this.defenders : this.attackers
    return teamIds.reduce((sum, id) => sum + (this.state.score[id] || 0), 0)
  }

  private getFreezeLevel(): number {
    if (!this.config.freezeEffectEnabled) return 0
    
    const defenderScore = this.getTeamScore('defender')
    const attackerScore = this.getTeamScore('attacker')
    const total = defenderScore + attackerScore
    
    if (total === 0) return 0
    
    const defenderRatio = defenderScore / total
    const attackerRatio = attackerScore / total
    
    // Determine which team is behind
    const behindRatio = Math.min(defenderRatio, attackerRatio)
    const percentage = behindRatio * 100
    
    if (percentage < this.config.freezeThresholds.high) return 3
    if (percentage < this.config.freezeThresholds.medium) return 2
    if (percentage < this.config.freezeThresholds.low) return 1
    
    return 0
  }

  getState(): GameState {
    return this.state
  }
}

/**
 * Free Mode Engine
 */
export class FreeModeEngine {
  private state: GameState
  private config: {
    roundDuration: number
    targetScore: number
    showRoundNumber: boolean
  }
  private currentAnchorIndex: number

  constructor(anchors: Anchor[], config: any) {
    this.config = config
    this.currentAnchorIndex = 0
    
    this.state = {
      mode: 'free',
      round: 1,
      score: {},
      anchors: anchors.map((a, index) => ({
        id: a.id,
        name: a.name,
        avatar: a.avatar,
        score: 0,
        rank: index + 1,
        isActive: !a.isEliminated,
        exclusiveGifts: a.exclusiveGifts,
        giftCount: 0,
      })),
      combo: 0,
      progress: 0,
      countdown: config.roundDuration,
      isRunning: false,
    }
    
    anchors.forEach(a => {
      this.state.score[a.id] = 0
    })
  }

  start(): GameState {
    this.state.isRunning = true
    this.state.startTime = Date.now()
    return this.state
  }

  pause(): GameState {
    this.state.isRunning = false
    return this.state
  }

  nextAnchor(): { state: GameState; event: GameEvent } {
    // Move to next anchor
    this.currentAnchorIndex = (this.currentAnchorIndex + 1) % this.state.anchors.length
    this.state.round++
    this.state.countdown = this.config.roundDuration
    
    const currentAnchor = this.state.anchors[this.currentAnchorIndex]
    
    const event: GameEvent = {
      eventType: 'ANCHOR_SWITCH',
      anchorId: currentAnchor.id,
    }
    
    return { state: this.state, event }
  }

  processGift(anchorId: string, giftValue: number, quantity: number = 1): { state: GameState; event: GameEvent } {
    const anchor = this.state.anchors.find(a => a.id === anchorId)
    if (!anchor || !anchor.isActive) {
      return { state: this.state, event: { eventType: 'GIFT_RECEIVED' } }
    }

    // Update score
    const points = giftValue * quantity
    anchor.score += points
    anchor.giftCount += quantity
    this.state.score[anchorId] = anchor.score

    // Update progress (towards target score)
    this.state.progress = Math.min(anchor.score / this.config.targetScore, 1)

    // Update ranks
    this.updateRanks()

    // Check if target reached
    const targetReached = anchor.score >= this.config.targetScore

    const event: GameEvent = {
      eventType: 'GIFT_RECEIVED',
      anchorId,
      quantity,
      effect: targetReached ? 'target_reached' : undefined,
    }

    return { state: this.state, event }
  }

  tickCountdown(): { state: GameState; event?: GameEvent } {
    if (!this.state.isRunning) {
      return { state: this.state }
    }

    if (this.state.countdown && this.state.countdown > 0) {
      this.state.countdown--
    }

    if (this.state.countdown === 0) {
      // Auto switch to next anchor
      return this.nextAnchor()
    }

    const event: GameEvent = {
      eventType: 'COUNTDOWN_TICK',
    }

    return { state: this.state, event }
  }

  private updateRanks() {
    const sorted = [...this.state.anchors]
      .filter(a => a.isActive)
      .sort((a, b) => b.score - a.score)
    
    sorted.forEach((anchor, index) => {
      anchor.rank = index + 1
    })
  }

  getCurrentAnchor(): GameAnchor {
    return this.state.anchors[this.currentAnchorIndex]
  }

  getState(): GameState {
    return this.state
  }
}
