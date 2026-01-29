# Changelog

## 1.14.1

### 🐛 Bug Fixes / 错误修复

- Fixed table sorting not restoring original order when switching back to unsorted state / 修复表格排序在切换回未排序状态时无法恢复原始顺序的问题
- Fixed color pattern not immediately reflecting in tables when clicking Apply in settings (now updates instantly without data refetch) / 修复在设置中点击应用时颜色模式未立即在表格中反映的问题（现在无需重新获取数据即可立即更新）
- Improved settings granularity: color pattern changes no longer trigger unnecessary data refetching / 改进设置粒度：颜色模式更改不再触发不必要的数据重新获取

## 1.14.0

### 🚀 Performance & Memory Optimizations / 性能和内存优化

- **Critical Memory Leak Fixes / 关键内存泄漏修复:**
  - Fixed message bus connection leaks in tool window (15+ connections per window now properly disposed) / 修复工具窗口消息总线连接泄漏（每个窗口15+连接现已正确释放）
  - Fixed project map memory leak (StockerApp instances now cleaned up on project close) / 修复项目映射内存泄漏（项目关闭时清理StockerApp实例）
  - Fixed HTTP response leaks (all responses now properly closed with automatic resource management) / 修复HTTP响应泄漏（所有响应现通过自动资源管理正确关闭）
  - Fixed table view disposal leak (static registry now properly cleaned up) / 修复表格视图释放泄漏（静态注册表现已正确清理）

- **HTTP & Network Improvements / HTTP和网络改进:**
  - Added connection timeouts (10s connect, 15s socket, 5s pool request) to prevent hanging threads / 添加连接超时（10秒连接，15秒套接字，5秒池请求）防止线程挂起
  - Properly close all HTTP connections with `.use{}` pattern / 使用`.use{}`模式正确关闭所有HTTP连接
  - Enhanced connection pool configuration / 增强连接池配置

- **Performance Optimizations / 性能优化:**
  - Consolidated scheduled tasks: reduced from 4 to 1 task (50% reduction in HTTP requests) / 合并计划任务：从4个减少到1个（HTTP请求减少50%）
  - Optimized table sorting: removed data duplication (50% memory reduction during sorting) / 优化表格排序：移除数据复制（排序时内存减少50%）
  - Reduced thread pool size from 4 to 1 threads (75% reduction) / 线程池大小从4减少到1（减少75%）
  - Implemented proper Disposable pattern for resource cleanup / 实现适当的Disposable模式进行资源清理

- **Architectural Improvements / 架构改进:**
  - Added ProjectManagerListener for automatic cleanup on project close / 添加ProjectManagerListener在项目关闭时自动清理
  - Improved encapsulation in StockerAppManager with proper public API / 改进StockerAppManager的封装与适当的公共API
  - Enhanced tool window lifecycle management / 增强工具窗口生命周期管理

## 1.13.1

- Add sortable table columns with three-state sorting (ascending, descending, unsorted) / 添加可排序的表格列，支持三态排序（升序、降序、不排序）

## 1.13.0

- Add customizable table column display settings / 添加可自定义的表格列显示设置

## 1.12.3

- Improve table selection clearing behavior

## 1.12.2

- Fix index names not obeying Pinyin display mode
- Add Hang Seng Tech Index (恒生科技指数, HSTECH)

## 1.12.1

- Add custom stock name feature with edit functionality in management dialog (custom names take highest priority)
- Enhanced management dialog UI with three-column layout (Code, Original Name, Custom Name)
- Enhanced suggestion dialog UI with improved search results layout

## 1.12.0

- Add Pinyin support for stock names with display settings
- Enhanced welcome and release note notifications
- Various technical improvements and dependency updates

## 1.11.1

- Fix IntelliJ 2024.2 series compatibility issues

## 1.11.0

- Fix IntelliJ 2023.3 series compatibility issues

## 1.10.2

- Fix compiler warnings

## 1.10.1

- Add A-Share Convertible Bond support

## 1.10.0

- Bring back SINA provider support

## 1.9.1

- Fix three digits price accuracy issue

## 1.9.0

- New management dialog: batch delete & reorder symbols

## 1.8.1

- Fix compatibility issue

## 1.8.0

- Support JetBrains 2022 EAP

## 1.7.0

- Replace Sina API with Tencent API due to Sina API is closed
- Crypto support is temporary removed since Sina API is no longer available

## 1.6.1

- Support JetBrains 2021.3 series

## 1.6.0

- Enhanced setting window UI
- Enhanced search dialog UI
- Enhanced management dialog UI

## 1.5.3

- Fixed multiple projects compatibility [#12](https://github.com/WhiteVermouth/intellij-investor-dashboard/issues/12)
- Fixed API compatibility

## 1.5.2

- Support IntelliJ 2021.2 EAP

## 1.5.1

- Fixed price accuracy [#11](https://github.com/WhiteVermouth/intellij-investor-dashboard/issues/11)

## 1.5.0

- New action: Stop refresh
- New pane: Crypto
- Deprecated: Tencent API

## 1.4.4

- Fix Long stock name wrapping
- Fix search bar text change event

## 1.4.3

- Fixed Android Studio compatibility
- Fixed missed ETF in search results

## 1.4.2

- Fix compatibility issue

## 1.4.1

- Enhanced stock management dialogs

## 1.4.0

- New Stock Add Dialog
- New Stock Delete Dialog
- Some enhancement and bug fix

## 1.3.7

- Support JetBrains 2019 series

## 1.3.6

- Add backward compatibility until 2020.1

## 1.3.5

- Fixed compatibility issue

## 1.3.4

- Support disable Red/Green color pattern

## 1.3.3

- Bug fix

## 1.3.2

- Bug fix

## 1.3.1

- Add right-click popup menu to delete code(s)

## 1.3.0

- Add index view

## 1.2.1

- Enhanced UI
- Bug fix

## 1.2.0

- Add a tab: ALL
- Enhanced UI

## 1.1.0

- Adopt more distinct colors
- Improve Last Update At datetime
- Add a new quote provider: Tencent

## 1.0.0

- Stocker: a stock quote dashboard
