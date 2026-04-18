# Burp Suite 扩展 - Clean

一个轻量级的 Burp Suite 扩展，在繁杂的数据包中，**快速聚焦重要的 URL 和请求参数/请求体**。

自动解析、美化、展开 JSON 数据，识别并突出 URL，让你在大量加密、嵌套的数据中快速找到关键信息。

## 核心价值

- 🎯 **从噪音中提取信息** — URL 编码、嵌套转义、Unicode 等多层包装一键展开
- 🔗 **URL 快速定位** — 鼠标悬停自动高亮，Ctrl+点击直接浏览器打开
- 📝 **参数一目了然** — GET/POST 参数、JSON 嵌套关系清晰可读

## 功能特性

### 1. JSON 美化与解析
- 自动识别并美化 JSON 数据（请求和响应）
- **嵌套 JSON 字符串展开**：如 `"p7":"{\"x\":1}"` 自动解析为嵌套对象并缩进展示
- 递归 URL 解码：`ba=%7B%22action%22%3A...%7D` 自动解码并美化其中的 JSON
- **Unicode 解码**：`\u4e2d\u6587` 自动转换为可读的汉字

### 2. 表单数据处理
- GET query 参数识别和美化：`a=1&b=2` 拆分为多行易读格式
- 表单字段若是 JSON 则自动展开美化
- URL 完全解码支持多层嵌套

### 3. URL 识别与浏览
- **自动识别文本中的 URL**（包括 JSON 字符串内）
- **Ctrl+鼠标悬停**：URL 自动下划线高亮
- **Ctrl+点击**：直接在浏览器打开 URL
- **右键菜单**："用浏览器打开 URL" 和 "复制 URL" 快速操作

### 4. 编辑体验优化
- ✅ **Ctrl+A** 全选 / **Ctrl+C** 复制 / **Ctrl+V** 粘贴 / **Ctrl+X** 剪切
- **Ctrl+滚轮**：字体放缩（10-28pt）
- **多主题支持**：dark（默认）/ monokai / vs / idea / eclipse 等
- 超长字符串智能截断（99 字符）并在 tooltip 显示完整内容

### 5. 架构设计
模块化结构便于维护和扩展：
- `processor/` — JSON/表单/Unicode 处理
- `ui/` — 自定义文本域、主题管理、URL 弹出菜单
- `editor/` — 请求/响应编辑器
- `resources/` — 配色配置（dark.properties）

## 编译和加载

### 编译
```bash
./gradlew build
```
产物在 `build/libs/Clean.jar`

### 在 Burp 中加载
1. Extensions → Installed → Add
2. Select file → 选择 `Clean.jar`
3. 在请求/响应标签页会出现 "Clean" 标签页

## 效果示例

### 示例 1：URL 编码的 JSON 参数

**原始 POST body：**
```
ba=%7B%22action%22%3A%22lifecycle-resume-upload-show%22%2C%22p1%22%3A1%2C%22p2%22%3A0%2C%22p3%22%3A1%7D
```

**Clean 扩展显示：**
```
ba = 
{
  "action": "lifecycle-resume-upload-show",
  "p1": 1,
  "p2": 0,
  "p3": 1
}
```

### 示例 2：嵌套转义 JSON 自动展开

**原始字段：**
```json
"p7":"{\"errorType\":\"collectData\",\"sceneType\":\"聊天埋点收集\",\"apiParam\":\"{\\\"isPresence\\\":1,\\\"mid\\\":\\\"324311367046405,324730435675401\\\"}\"}"
```

**Clean 扩展展开后：**
```json
"p7": {
  "errorType": "collectData",
  "sceneType": "聊天埋点收集",
  "apiParam": {
    "isPresence": 1,
    "mid": "324311367046405,324730435675401"
  }
}
```

### 示例 3：Unicode 自动解码

**原始：**
```
\u4e2d\u6587\u6d4b\u8bd5 {"name":"\u5f20\u4e09","age":28}
```

**Clean 扩展显示：**
```
中文测试 {
  "name": "张三",
  "age": 28
}
```

### 示例 4：URL 快速打开

在 JSON 中看到：
```json
"userAvatar":"https://img.test.com/user/avatar/avatar_1.png"
```

- 按住 Ctrl，鼠标移到 URL 上 → 自动下划线高亮
- Ctrl+左键点击 → 浏览器打开图片
- 或右键点击 → 选菜单 "用浏览器打开 URL"

## 配置

主题和配色在 `src/main/resources/theme/dark.properties`：
```properties
background=#2b2b2b
foreground=#c8d7ec
token.string=#95bf5e
token.number.int=#6eb1ff
# ... 更多配色
```

修改后重新编译生效。

## 快捷键速查

| 快捷键 | 功能 |
|---|---|
| Ctrl+A | 全选 |
| Ctrl+C | 复制 |
| Ctrl+V | 粘贴 |
| Ctrl+X | 剪切 |
| Ctrl+滚轮 | 字体放缩 |
| Ctrl+鼠标悬停 | URL 高亮 |
| Ctrl+点击 | 浏览器打开 URL |
| 右键 | 菜单（复制/粘贴/打开 URL 等） |


