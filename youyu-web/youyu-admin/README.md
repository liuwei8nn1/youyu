# YouYu Admin

基于 Vue3 + Element Plus 的后台管理系统前端项目。

## 技术栈

- Vue 3 - 渐进式 JavaScript 框架
- Vite - 下一代前端构建工具
- Element Plus - 基于 Vue 3 的组件库
- Vue Router - 官方路由管理器
- Pinia - Vue 官方状态管理库
- Axios - HTTP 客户端

## 项目结构

```
youyu-admin/
├── src/
│   ├── api/              # API 接口定义
│   │   └── auth.js       # 认证相关接口
│   ├── layout/           # 布局组件
│   │   ├── components/
│   │   │   └── SidebarItem.vue  # 侧边栏菜单项
│   │   └── index.vue     # 主布局
│   ├── mock/             # Mock 数据
│   │   ├── auth.js       # 认证相关 Mock
│   │   └── index.js      # Mock 拦截器
│   ├── router/           # 路由配置
│   │   └── index.js      # 路由定义和守卫
│   ├── stores/           # Pinia 状态管理
│   │   └── user.js       # 用户状态
│   ├── utils/            # 工具函数
│   │   └── request.js    # Axios 封装（支持 token 刷新）
│   ├── views/            # 页面组件
│   │   ├── dashboard/    # 首页
│   │   ├── error/        # 错误页面
│   │   └── login/        # 登录页
│   ├── App.vue           # 根组件
│   └── main.js           # 入口文件
├── index.html            # HTML 模板
├── vite.config.js        # Vite 配置
└── package.json          # 项目依赖
```

## 功能特性

### 1. 用户认证
- 登录/登出
- Token 自动刷新
- 路由守卫保护

### 2. 动态路由
- 从后端接口获取菜单数据
- 动态生成路由
- 权限控制

### 3. Mock 数据
- 所有接口数据均已 Mock
- 无需启动后端服务即可开发
- 可轻松切换到真实接口

## 开发指南

### 安装依赖

```bash
npm install
```

### 启动开发服务器

```bash
npm run dev
```

访问 http://localhost:3000

### 构建生产版本

```bash
npm run build
```

### 预览生产构建

```bash
npm run preview
```

## API 接口

### 认证接口

#### 登录
- **URL**: `/api/auth/login`
- **Method**: `POST`
- **Request Body**:
  ```json
  {
    "username": "admin",
    "password": "123456"
  }
  ```
- **Response**:
  ```json
  {
    "code": 200,
    "message": "success",
    "data": {
      "accessToken": "xxx",
      "refreshToken": "xxx",
      "userId": 1,
      "username": "admin",
      "userType": 1,
      "roles": ["admin"]
    }
  }
  ```

#### 刷新 Token
- **URL**: `/api/auth/refresh`
- **Method**: `POST`
- **Query Params**: `refreshToken=xxx`
- **Response**:
  ```json
  {
    "code": 200,
    "message": "success",
    "data": {
      "accessToken": "xxx",
      "refreshToken": "xxx"
    }
  }
  ```

#### 获取用户菜单
- **URL**: `/api/auth/menus`
- **Method**: `GET`
- **Headers**: `Authorization: Bearer {accessToken}`
- **Response**:
  ```json
  {
    "code": 200,
    "message": "success",
    "data": [
      {
        "id": 1,
        "parentId": 0,
        "name": "系统管理",
        "path": "/system",
        "component": "Layout",
        "icon": "Setting",
        "type": "MENU",
        "visible": 1,
        "status": 1,
        "children": [...]
      }
    ]
  }
  ```

## 切换真实接口

当后端服务就绪后，只需修改 `src/mock/index.js`：

```javascript
// 将 ENABLE_MOCK 改为 false
const ENABLE_MOCK = false
```

同时确保 `vite.config.js` 中的代理配置正确指向后端服务地址。

## 注意事项

1. 默认启用 Mock 数据，无需启动后端服务
2. Token 存储在 localStorage 中
3. 401 错误会自动尝试刷新 Token
4. 刷新失败会跳转到登录页
5. 菜单数据由后端接口返回，前端动态生成路由
