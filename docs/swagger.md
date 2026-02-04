# Swagger / OpenAPI 使用说明

## 访问地址
- Swagger UI：http://localhost:{port}{context-path}/swagger-ui/index.html
- OpenAPI JSON：{context-path}/v3/api-docs
- 分组示例：{context-path}/v3/api-docs/auth

> 当 `server.servlet.context-path=/api` 时，实际地址应为 `http://localhost:{port}/api/swagger-ui/index.html` 等。

## JWT 授权方式
1. 打开 Swagger UI。
2. 点击右上角 **Authorize**。
3. 在 `bearerAuth` 中输入 Token：
   - 如果 UI 自动添加 `Bearer ` 前缀：只输入 `<JWT>`
   - 如果未自动添加：输入 `Bearer <JWT>`
4. 点击 **Authorize** 保存后即可调用受保护接口。

## 验收自检
- UI 页面能打开：`{context-path}/swagger-ui/index.html`
- OpenAPI JSON 能访问：`{context-path}/v3/api-docs`
- 分组 JSON 能访问：例如 `{context-path}/v3/api-docs/auth`
- 未授权调用受保护接口返回 401；授权后调用成功

