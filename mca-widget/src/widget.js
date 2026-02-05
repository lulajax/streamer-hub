/**
 * MCA Widget - Consumer for TikTok Live Studio Browser Source
 * 
 * Features:
 * - Connects to WebSocket as consumer
 * - Receives state snapshots and events from server
 * - Renders different game modes (sticker_dance, attack_defense, free)
 * - No local state authority - pure rendering
 */

(function() {
    'use strict';

    // Configuration
    const RECONNECT_DELAYS = [1000, 2000, 4000, 8000];
    const HEARTBEAT_INTERVAL = 30000;

    // Parse URL parameters
    const urlParams = new URLSearchParams(window.location.search);
    const roomId = urlParams.get('roomId');
    const token = urlParams.get('token');
    const widgetView = urlParams.get('view') || 'preview';
    const apiBase = resolveApiBase(urlParams);
    const ROOM_WS_URL = buildWsUrl(apiBase, '/ws/room');
    const useWidgetTokenMode = !roomId && !!token;

    // State
    let ws = null;
    let reconnectAttempt = 0;
    let reconnectTimeout = null;
    let heartbeatInterval = null;
    let isJoined = false;
    let widgetWsConnected = false;
    let lastSeq = 0;
    let currentMode = null;
    let currentState = null;
    let currentWidgetSettings = null;

    // DOM Elements
    const app = document.getElementById('app');

    // Logger
    function log(level, ...args) {
        const prefix = `[MCA Widget][${level.toUpperCase()}]`;
        console[level](prefix, ...args);
    }

    // Initialize
    function init() {
        log('info', 'Initializing MCA Widget');
        log('info', 'Room ID:', roomId);

        if (roomId) {
            if (!token) {
                showError('Missing token parameter in URL');
                return;
            }
            connect();
            return;
        }

        if (token) {
            connectWidgetSocket();
            return;
        }

        showError('Missing roomId or token parameter in URL');
    }

    // Connect to WebSocket
    function connect() {
        log('info', 'Connecting to WebSocket...');
        showLoading('Connecting...');

        try {
            ws = new WebSocket(ROOM_WS_URL);

            ws.onopen = handleOpen;
            ws.onmessage = handleMessage;
            ws.onclose = handleClose;
            ws.onerror = handleError;

        } catch (err) {
            log('error', 'Failed to create WebSocket:', err);
            scheduleReconnect(connect);
        }
    }

    // Connect to Widget WebSocket (token mode)
    function connectWidgetSocket() {
        log('info', 'Connecting to Widget WebSocket...');
        showLoading('Connecting...');

        const widgetWsUrl = buildWsUrl(
            apiBase,
            `/ws/widget/${encodeURIComponent(token)}`,
            { view: widgetView }
        );

        try {
            ws = new WebSocket(widgetWsUrl);

            ws.onopen = handleWidgetOpen;
            ws.onmessage = handleWidgetMessage;
            ws.onclose = handleWidgetClose;
            ws.onerror = handleWidgetError;
        } catch (err) {
            log('error', 'Failed to create Widget WebSocket:', err);
            scheduleReconnect(connectWidgetSocket);
        }
    }

    function handleWidgetOpen() {
        log('info', 'Widget WebSocket connected');
        reconnectAttempt = 0;
        widgetWsConnected = true;
    }

    function handleWidgetMessage(event) {
        try {
            const message = JSON.parse(event.data);
            log('debug', 'Received widget message:', message.type);

            switch (message.type) {
                case 'widget_data':
                    handleWidgetData(message);
                    break;
                case 'error':
                    log('error', 'Widget server error:', message.error);
                    showError(message.error);
                    break;
                default:
                    log('debug', 'Ignoring widget message type:', message.type);
            }
        } catch (err) {
            log('error', 'Failed to parse widget message:', err);
        }
    }

    function handleWidgetClose(event) {
        log('info', 'Widget WebSocket closed:', event.code, event.reason);
        widgetWsConnected = false;

        if (event.code !== 1000 && event.code !== 1001) {
            scheduleReconnect(connectWidgetSocket);
        }
    }

    function handleWidgetError(error) {
        log('error', 'Widget WebSocket error:', error);
    }

    // Handle WebSocket open
    function handleOpen() {
        log('info', 'WebSocket connected');
        reconnectAttempt = 0;

        // Send join message
        sendMessage({
            type: 'join',
            role: 'consumer',
            roomId: roomId,
            token: token
        });

        // Start heartbeat
        startHeartbeat();
    }

    // Handle WebSocket message
    function handleMessage(event) {
        try {
            const message = JSON.parse(event.data);
            log('debug', 'Received message:', message.type);

            switch (message.type) {
                case 'join_success':
                    handleJoinSuccess(message);
                    break;
                case 'snapshot':
                    handleSnapshot(message);
                    break;
                case 'state':
                    handleState(message);
                    break;
                case 'event':
                    handleEvent(message);
                    break;
                case 'waiting':
                    showWaiting(message.message || 'Waiting for producer');
                    break;
                case 'producer_offline':
                    showWaiting('Producer disconnected');
                    break;
                case 'heartbeat':
                    // Heartbeat response, ignore
                    break;
                case 'error':
                    log('error', 'Server error:', message.error);
                    showError(message.error);
                    break;
                default:
                    log('warn', 'Unknown message type:', message.type);
            }
        } catch (err) {
            log('error', 'Failed to parse message:', err);
        }
    }

    // Handle WebSocket close
    function handleClose(event) {
        log('info', 'WebSocket closed:', event.code, event.reason);
        isJoined = false;
        stopHeartbeat();

        if (event.code !== 1000 && event.code !== 1001) {
            scheduleReconnect(connect);
        }
    }

    // Handle WebSocket error
    function handleError(error) {
        log('error', 'WebSocket error:', error);
    }

    // Handle join success
    function handleJoinSuccess(message) {
        log('info', 'Joined room successfully:', message.roomId);
        isJoined = true;
    }

    // Handle snapshot (full state on join/reconnect)
    function handleSnapshot(message) {
        log('info', 'Received snapshot, seq:', message.seq);
        
        lastSeq = message.seq || 0;
        currentState = message.payload;
        currentMode = currentState?.mode;
        
        render();
    }

    // Handle state update
    function handleState(message) {
        const seq = message.seq || 0;
        
        // Discard out-of-order messages
        if (seq <= lastSeq) {
            log('debug', 'Discarding out-of-order state, seq:', seq, 'lastSeq:', lastSeq);
            return;
        }
        
        lastSeq = seq;
        currentState = message.payload;
        currentMode = currentState?.mode;
        
        render();
    }

    // Handle event
    function handleEvent(message) {
        log('info', 'Received event:', message.payload?.eventType);
        
        // Play event animation
        playEventAnimation(message.payload);
    }

    // Handle widget data (token mode)
    function handleWidgetData(message) {
        const data = message.payload;
        currentWidgetSettings = parseWidgetSettings(data?.widgetSettings);
        currentState = normalizeWidgetData(data, currentWidgetSettings);
        currentMode = currentState?.mode;

        render();
    }

    // Schedule reconnection
    function scheduleReconnect(connectFn) {
        const delay = RECONNECT_DELAYS[Math.min(reconnectAttempt, RECONNECT_DELAYS.length - 1)];
        reconnectAttempt++;
        
        log('info', `Reconnecting in ${delay}ms (attempt ${reconnectAttempt})`);
        showLoading(`Reconnecting... (${reconnectAttempt})`);
        
        reconnectTimeout = setTimeout(() => {
            connectFn();
        }, delay);
    }

    // Start heartbeat
    function startHeartbeat() {
        heartbeatInterval = setInterval(() => {
            sendMessage({
                type: 'heartbeat',
                ts: Date.now()
            });
        }, HEARTBEAT_INTERVAL);
    }

    // Stop heartbeat
    function stopHeartbeat() {
        if (heartbeatInterval) {
            clearInterval(heartbeatInterval);
            heartbeatInterval = null;
        }
    }

    // Send message
    function sendMessage(message) {
        if (ws && ws.readyState === WebSocket.OPEN) {
            ws.send(JSON.stringify(message));
            return true;
        }
        return false;
    }

    // Render current state
    function render() {
        if (!currentState) {
            showWaiting('Waiting for data...');
            return;
        }

        switch (currentMode) {
            case 'sticker_dance':
                renderStickerMode();
                break;
            case 'attack_defense':
                renderPKMode();
                break;
            case 'free':
                renderFreeMode();
                break;
            default:
                showWaiting('Unknown game mode');
        }
    }

    // Render Sticker Dance Mode
    function renderStickerMode() {
        const { anchors, countdown, combo } = currentState;
        
        if (!anchors || anchors.length === 0) {
            showWaiting('No anchors available');
            return;
        }

        // Sort by score descending
        const sortedAnchors = [...anchors].sort((a, b) => b.score - a.score);
        
        let html = '<div class="sticker-mode">';
        
        // Countdown display
        if (countdown !== undefined && countdown > 0) {
            html += `<div class="sticker-countdown">${formatTime(countdown)}</div>`;
        }
        
        // Combo display
        if (combo > 1) {
            html += `<div style="font-size: 24px; color: #fbbf24; font-weight: 700;">COMBO x${combo}</div>`;
        }
        
        // Sticker grid
        html += '<div class="sticker-grid">';
        
        sortedAnchors.forEach((anchor, index) => {
            const isTop = index === 0 && anchor.score > 0;
            html += `
                <div class="sticker-item ${isTop ? 'active' : ''}">
                    <img class="sticker-avatar" src="${anchor.avatar || 'https://via.placeholder.com/64'}" alt="${anchor.name}">
                    <div class="sticker-name">${anchor.name}</div>
                    <div class="sticker-score">${formatNumber(anchor.score)}</div>
                </div>
            `;
        });
        
        html += '</div>';
        html += renderConnectionStatus();
        html += '</div>';
        
        app.innerHTML = html;
    }

    // Render PK Mode
    function renderPKMode() {
        const { anchors, progress = 0.5 } = currentState;
        
        if (!anchors || anchors.length < 2) {
            showWaiting('Need at least 2 anchors for PK mode');
            return;
        }

        // Split into defender and attacker teams
        // Assuming first half are defenders, second half are attackers
        const mid = Math.ceil(anchors.length / 2);
        const defenders = anchors.slice(0, mid);
        const attackers = anchors.slice(mid);
        
        const defenderScore = defenders.reduce((sum, a) => sum + a.score, 0);
        const attackerScore = attackers.reduce((sum, a) => sum + a.score, 0);
        
        const defenderPercent = Math.round(progress * 100);
        const attackerPercent = 100 - defenderPercent;
        
        let html = '<div class="pk-mode">';
        
        // Header with scores
        html += `
            <div class="pk-header">
                <div class="pk-team defender">
                    <div class="pk-team-name">DEFENDER</div>
                    <div class="pk-team-score">${formatNumber(defenderScore)}</div>
                </div>
                <div class="pk-vs">VS</div>
                <div class="pk-team attacker">
                    <div class="pk-team-name">ATTACKER</div>
                    <div class="pk-team-score">${formatNumber(attackerScore)}</div>
                </div>
            </div>
        `;
        
        // Progress bar
        html += `
            <div class="pk-progress-container">
                <div class="pk-progress-bar">
                    <div class="pk-progress-defender" style="width: ${defenderPercent}%"></div>
                    <div class="pk-progress-attacker" style="width: ${attackerPercent}%"></div>
                </div>
                <div class="pk-progress-text">${defenderPercent}% - ${attackerPercent}%</div>
            </div>
        `;
        
        // Freeze effect overlay
        const freezeLevel = getFreezeLevel(progress);
        if (freezeLevel > 0) {
            html += `<div class="freeze-effect level-${freezeLevel} active"></div>`;
        }
        
        html += renderConnectionStatus();
        html += '</div>';
        
        app.innerHTML = html;
    }

    // Render Free Mode
    function renderFreeMode() {
        const { anchors, countdown, progress = 0 } = currentState;
        
        if (!anchors || anchors.length === 0) {
            showWaiting('No anchors available');
            return;
        }

        // Find current anchor (first active one or highest score)
        const currentAnchor = anchors.find(a => a.isActive) || anchors[0];
        
        let html = '<div class="free-mode">';
        
        // Current anchor display
        html += `
            <div class="free-current-anchor">
                <img class="free-anchor-avatar" src="${currentAnchor.avatar || 'https://via.placeholder.com/100'}" alt="${currentAnchor.name}">
                <div class="free-anchor-name">${currentAnchor.name}</div>
                <div class="free-anchor-score">${formatNumber(currentAnchor.score)}</div>
            </div>
        `;
        
        // Progress bar
        html += `
            <div class="free-progress-container">
                <div class="free-progress-bar" style="width: ${Math.round(progress * 100)}%"></div>
            </div>
        `;
        
        // Countdown
        if (countdown !== undefined && countdown > 0) {
            html += `<div class="free-countdown">${formatTime(countdown)}</div>`;
        }
        
        // Leaderboard
        const sortedAnchors = [...anchors].sort((a, b) => b.score - a.score).slice(0, 5);
        html += '<div class="leaderboard">';
        sortedAnchors.forEach((anchor, index) => {
            html += `
                <div class="leaderboard-item">
                    <div class="leaderboard-rank rank-${index + 1}">${index + 1}</div>
                    <img class="leaderboard-avatar" src="${anchor.avatar || 'https://via.placeholder.com/36'}" alt="${anchor.name}">
                    <div class="leaderboard-name">${anchor.name}</div>
                    <div class="leaderboard-score">${formatNumber(anchor.score)}</div>
                </div>
            `;
        });
        html += '</div>';
        
        html += renderConnectionStatus();
        html += '</div>';
        
        app.innerHTML = html;
    }

    // Play event animation
    function playEventAnimation(event) {
        if (!event) return;
        
        const { eventType, effect, giftId, userName } = event;
        
        // Create animation element
        const animEl = document.createElement('div');
        animEl.className = 'event-animation';
        animEl.style.left = `${Math.random() * 80 + 10}%`;
        animEl.style.top = `${Math.random() * 50 + 25}%`;
        
        switch (eventType) {
            case 'GIFT_RECEIVED':
                animEl.innerHTML = `
                    <div class="gift-effect">🎁</div>
                    <div style="font-size: 14px; color: #fbbf24; font-weight: 600;">${userName || 'Someone'}</div>
                `;
                break;
            case 'COMBO_TRIGGER':
                animEl.innerHTML = `
                    <div style="font-size: 32px; color: #ef4444; font-weight: 800;">COMBO!</div>
                `;
                break;
            default:
                return;
        }
        
        document.body.appendChild(animEl);
        
        // Remove after animation
        setTimeout(() => {
            animEl.remove();
        }, 2000);
    }

    // Get freeze level based on progress
    function getFreezeLevel(progress) {
        // Progress is 0-1, 0.5 is balanced
        const imbalance = Math.abs(progress - 0.5) * 2; // 0 to 1
        
        if (imbalance > 0.6) return 3;
        if (imbalance > 0.4) return 2;
        if (imbalance > 0.2) return 1;
        return 0;
    }

    // Render connection status indicator
    function renderConnectionStatus() {
        let statusClass = 'disconnected';
        let statusText = 'Disconnected';

        if (useWidgetTokenMode) {
            statusClass = widgetWsConnected ? 'connected' : ws?.readyState === WebSocket.CONNECTING ? 'connecting' : 'disconnected';
            statusText = widgetWsConnected ? 'Connected' : ws?.readyState === WebSocket.CONNECTING ? 'Connecting...' : 'Disconnected';
        } else {
            statusClass = isJoined ? 'connected' : ws?.readyState === WebSocket.CONNECTING ? 'connecting' : 'disconnected';
            statusText = isJoined ? 'Connected' : ws?.readyState === WebSocket.CONNECTING ? 'Connecting...' : 'Disconnected';
        }
        
        return `
            <div class="connection-status">
                <div class="connection-dot ${statusClass}"></div>
                <span>${statusText}</span>
            </div>
        `;
    }

    // Show loading state
    function showLoading(message) {
        app.innerHTML = `
            <div class="loading">
                <div class="loading-spinner"></div>
                <div class="loading-text">${message}</div>
            </div>
        `;
    }

    // Show waiting state
    function showWaiting(message) {
        app.innerHTML = `
            <div class="waiting">
                <div style="font-size: 48px;">⏳</div>
                <div class="waiting-text">${message}</div>
                ${renderConnectionStatus()}
            </div>
        `;
    }

    // Show error state
    function showError(message) {
        app.innerHTML = `
            <div class="error">
                <div class="error-icon">⚠️</div>
                <div class="error-text">${message}</div>
            </div>
        `;
    }

    // Format number with commas
    function formatNumber(num) {
        if (num === undefined || num === null) return '0';
        return num.toLocaleString();
    }

    // Format time (seconds to MM:SS)
    function formatTime(seconds) {
        if (seconds === undefined || seconds === null) return '00:00';
        const mins = Math.floor(seconds / 60);
        const secs = seconds % 60;
        return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
    }

    // Resolve API base URL
    function resolveApiBase(params) {
        const paramBase = params.get('apiBase') || params.get('api');
        if (paramBase) {
            return trimTrailingSlash(paramBase);
        }

        const protocol = window.location.protocol;
        const hostname = window.location.hostname;
        const port = window.location.port;

        if (port && port !== '80' && port !== '443') {
            if (port !== '8080') {
                return `${protocol}//${hostname}:8080/api`;
            }
        }

        return `${protocol}//${window.location.host}/api`;
    }

    // Build WebSocket URL from API base
    function buildWsUrl(baseUrl, wsPath, params) {
        try {
            const apiUrl = new URL(baseUrl);
            apiUrl.protocol = apiUrl.protocol === 'https:' ? 'wss:' : 'ws:';
            const basePath = apiUrl.pathname.replace(/\/$/, '');
            const path = wsPath.startsWith('/') ? wsPath : `/${wsPath}`;
            apiUrl.pathname = `${basePath}${path}`;
            apiUrl.search = '';
            if (params) {
                Object.entries(params).forEach(([key, value]) => {
                    if (value !== undefined && value !== null && value !== '') {
                        apiUrl.searchParams.set(key, value);
                    }
                });
            }
            apiUrl.hash = '';
            return apiUrl.toString();
        } catch (err) {
            const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
            const path = wsPath && wsPath.startsWith('/') ? wsPath : `/ws/room`;
            return `${protocol}//${window.location.host}${path}`;
        }
    }

    function trimTrailingSlash(url) {
        return url.endsWith('/') ? url.slice(0, -1) : url;
    }

    function parseWidgetSettings(settingsJson) {
        if (!settingsJson) return null;
        try {
            return JSON.parse(settingsJson);
        } catch (err) {
            log('warn', 'Failed to parse widget settings JSON:', err);
            return null;
        }
    }

    function normalizeWidgetData(data, settings) {
        if (!data) return null;

        const mode = mapGameMode(data.gameMode);
        const anchors = normalizeAnchors(data.anchors, settings);
        const progress = computeProgress(mode, anchors);

        return {
            mode,
            anchors,
            progress,
            countdown: data.countdown,
            combo: data.combo
        };
    }

    function mapGameMode(gameMode) {
        if (!gameMode) return null;
        const value = String(gameMode).toUpperCase();
        switch (value) {
            case 'STICKER':
                return 'sticker_dance';
            case 'PK':
                return 'attack_defense';
            case 'FREE':
                return 'free';
            default:
                return null;
        }
    }

    function normalizeAnchors(anchors, settings) {
        if (!Array.isArray(anchors)) {
            return [];
        }

        let mapped = anchors.map((anchor) => ({
            name: anchor?.name || 'Unknown',
            avatar: anchor?.avatarUrl || anchor?.avatar || '',
            score: anchor?.totalScore ?? anchor?.score ?? 0,
            isActive: anchor?.isActive !== false,
            displayOrder: anchor?.displayOrder
        }));

        const maxItems = settings?.display?.maxItems;
        if (maxItems && Number.isFinite(maxItems)) {
            mapped = mapped.slice(0, Math.max(0, maxItems));
        }

        return mapped;
    }

    function computeProgress(mode, anchors) {
        if (mode !== 'attack_defense') {
            return 0;
        }

        if (!anchors || anchors.length < 2) {
            return 0.5;
        }

        const mid = Math.ceil(anchors.length / 2);
        const defenders = anchors.slice(0, mid);
        const attackers = anchors.slice(mid);
        const defenderScore = defenders.reduce((sum, a) => sum + (a.score || 0), 0);
        const attackerScore = attackers.reduce((sum, a) => sum + (a.score || 0), 0);
        const total = defenderScore + attackerScore;

        if (total === 0) {
            return 0.5;
        }

        return defenderScore / total;
    }

    // Start
    init();

    // Cleanup on page unload
    window.addEventListener('beforeunload', function() {
        if (ws) {
            ws.close();
        }
        if (reconnectTimeout) {
            clearTimeout(reconnectTimeout);
        }
    });

})();
