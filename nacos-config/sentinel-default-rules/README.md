# Sentinel 默认规则配置说明

## 目录结构

```
sentinel-default-rules/
├── flow-rules.json          # 流控规则
├── degrade-rules.json       # 降级规则
├── system-rules.json        # 系统规则
├── authority-rules.json     # 授权规则
└── param-flow-rules.json    # 热点参数规则
```

## 配置加载流程

1. **服务启动时**：从 `classpath:sentinel-default-rules/` 读取默认配置
2. **检查 Nacos**：如果 Nacos 中已存在对应配置，则跳过发布
3. **发布到 Nacos**：如果 Nacos 中不存在，则将本地默认配置发布到 Nacos
4. **优先级**：Nacos 配置 > 本地默认配置

## 字段说明

### 流控规则 (flow-rules.json)

| 字段 | 类型 | 说明 | 示例值 |
|------|------|------|--------|
| resource | String | 资源名称（接口路径或方法名） | "hello" |
| limitApp | String | 流控应用来源 | "default" |
| grade | Integer | 阈值类型：0=线程数，1=QPS | 1 |
| count | Double | 阈值数量 | 10 |
| strategy | Integer | 流控模式：0=直接，1=关联，2=链路 | 0 |
| controlBehavior | Integer | 流控效果：0=快速失败，1=Warm Up，2=排队等待 | 0 |
| clusterMode | Boolean | 是否集群模式 | false |

**grade 取值说明**：
- 0：线程数限流
- 1：QPS 限流（每秒请求数）

**strategy 取值说明**：
- 0：直接流控
- 1：关联流控（当关联资源达到阈值时，限制自己）
- 2：链路流控（只记录指定链路上的流量）

**controlBehavior 取值说明**：
- 0：快速失败（立即拒绝）
- 1：Warm Up（预热模式，逐步增加阈值）
- 2：排队等待（匀速排队）

### 降级规则 (degrade-rules.json)

| 字段 | 类型 | 说明 | 示例值 |
|------|------|------|--------|
| resource | String | 资源名称 | "error" |
| grade | Integer | 降级策略：0=RT，1=异常比例，2=异常数 | 2 |
| count | Double | 阈值 | 0.5 |
| timeWindow | Integer | 时间窗口（秒） | 10 |
| minRequestAmount | Integer | 最小请求数 | 5 |
| statIntervalMs | Integer | 统计时长（毫秒） | 1000 |
| slowRatioThreshold | Double | 慢调用比例阈值（仅 RT 模式） | 0.5 |

**grade 取值说明**：
- 0：平均响应时间（RT）
- 1：异常比例（0.0-1.0）
- 2：异常数（每分钟异常数量）

### 系统规则 (system-rules.json)

系统规则是从整体维度对应用入口流量进行控制，保护系统不被过载。

| 字段 | 类型 | 说明 | 示例值 |
|------|------|------|--------|
| highestSystemLoad | Double | 最大系统负载（load1），仅对 Linux/Unix-like 系统生效 | -1（不启用） |
| highestCpuUsage | Double | 最大 CPU 使用率（0.0-1.0） | 0.9（90%） |
| avgRt | Long | 所有入口流量的平均响应时间（毫秒） | -1（不启用） |
| maxThread | Long | 所有入口流量的最大并发线程数 | -1（不启用） |
| qps | Double | 所有入口流量的最大 QPS | -1（不启用） |

**默认配置说明**：
```json
[
  {
    "highestSystemLoad": -1,    // 不启用系统负载限制
    "highestCpuUsage": 0.9,     // CPU 使用率超过 90% 时触发保护
    "avgRt": -1,                // 不启用平均 RT 限制
    "maxThread": -1,            // 不启用最大线程数限制
    "qps": -1                   // 不启用全局 QPS 限制
  }
]
```

**注意事项**：
- 系统规则是全局级别的，作用于所有入口流量
- 建议谨慎使用，避免误伤正常流量
- `-1` 表示不启用该规则
- **推荐配置**：生产环境建议只启用 `highestCpuUsage`（0.8-0.9），其他规则根据实际需求开启

### 授权规则 (authority-rules.json)

| 字段 | 类型 | 说明 | 示例值 |
|------|------|------|--------|
| resource | String | 资源名称 | "api" |
| limitApp | String | 授权应用，多个用逗号分隔 | "app1,app2" |
| strategy | Integer | 授权类型：0=白名单，1=黑名单 | 0 |

### 热点参数规则 (param-flow-rules.json)

| 字段 | 类型 | 说明 | 示例值 |
|------|------|------|--------|
| resource | String | 资源名称 | "hotApi" |
| grade | Integer | 阈值类型：0=线程数，1=QPS | 1 |
| paramIdx | Integer | 参数索引（从 0 开始） | 0 |
| count | Double | 阈值 | 100 |
| durationInSec | Long | 统计窗口时长（秒） | 1 |

## 使用示例

### 示例 1：为秒杀接口设置 QPS 限流

```json
[
  {
    "resource": "seckill",
    "limitApp": "default",
    "grade": 1,
    "count": 100,
    "strategy": 0,
    "controlBehavior": 0,
    "clusterMode": false
  }
]
```

### 示例 2：为错误接口设置异常比例降级

```json
[
  {
    "resource": "error",
    "grade": 2,
    "count": 0.5,
    "timeWindow": 10,
    "minRequestAmount": 5,
    "statIntervalMs": 1000,
    "slowRatioThreshold": 0.5
  }
]
```

## 开发调试流程

1. **本地开发**：修改 `resources/sentinel-default-rules/` 下的 JSON 文件
2. **重启服务**：配置会自动发布到 Nacos（如果 Nacos 中没有该配置）
3. **临时测试**：通过 Sentinel Dashboard 动态调整参数进行测试
4. **确认配置**：测试通过后，自行将最终配置更新到 Nacos
5. **版本管理**：将确认后的配置同步回本地 JSON 文件，提交 Git

## 官方文档参考

Sentinel 提供了丰富的功能和配置选项，以下是官方文档地址：

- **Sentinel 官方网站**: https://sentinelguard.io/
- **Sentinel GitHub Wiki（中文文档）**: https://github.com/alibaba/Sentinel/wiki/%E4%BB%8B%E7%BB%8D
- **流量控制**: https://github.com/alibaba/Sentinel/wiki/%E6%B5%81%E9%87%8F%E6%8E%A7%E5%88%B6
- **熔断降级**: https://github.com/alibaba/Sentinel/wiki/%E7%86%94%E6%96%AD%E9%99%8D%E7%BA%A7
- **系统自适应保护**: https://github.com/alibaba/Sentinel/wiki/%E7%B3%BB%E7%BB%9F%E8%87%AA%E9%80%82%E5%BA%94%E9%99%90%E6%B5%81
- **热点参数限流**: https://github.com/alibaba/Sentinel/wiki/%E7%83%AD%E7%82%B9%E5%8F%82%E6%95%B0%E9%99%90%E6%B5%81
- **黑白名单控制**: https://github.com/alibaba/Sentinel/wiki/%E9%BB%91%E7%99%BD%E5%90%8D%E5%8D%95%E6%8E%A7%E5%88%B6
- **集群流控**: https://github.com/alibaba/Sentinel/wiki/%E9%9B%86%E7%BE%A4%E6%B5%81%E6%8E%A7
- **网关流控**: https://github.com/alibaba/Sentinel/wiki/%E7%BD%91%E5%85%B3%E6%B5%81%E6%8E%A7
- **控制台使用指南**: https://github.com/alibaba/Sentinel/wiki/%E6%8E%A7%E5%88%B6%E5%8F%B0
- **动态规则配置**: https://github.com/alibaba/Sentinel/wiki/%E5%8A%A8%E6%80%81%E8%A7%84%E5%88%99%E6%89%A9%E5%B1%95

> 💡 **提示**: 
> - Sentinel Dashboard 上还有其他高级配置（如集群流控、网关流控等），如需使用请参考官方文档
> - 完整的文档列表请查看: https://github.com/alibaba/Sentinel/wiki

## 注意事项

- ⚠️ Nacos 中的配置优先级最高，本地配置仅在 Nacos 无配置时生效
- ⚠️ 修改本地配置后需要重启服务才能生效
- ⚠️ Sentinel Dashboard 的调整是临时的，重启后会恢复为 Nacos 配置
- ⚠️ JSON 格式不支持注释，请参考本文档了解字段含义
