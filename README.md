本 demo 为 [扫一扫](https://help.aliyun.com/document_detail/52596.html?spm=a2c4g.11186623.6.1511.616e3766QGv4RN) 单组件 demo，clone 后直接运行工程即可。

### 支持基线与接入方式
支持 inside 和 aar 两种接入方式，支持基线：

- 10.1.60，支持 inside 方式
- 10.1.68，支持 inside 和 aar 方式

demo中默认使用10.1.68基线。

### 切换接入方式
- demo 中通过 gradle.properties 文件中 mPaasBuildType 字段区分接入方式，默认aar，可修改为inside（等号前后不要有空格）。
- app/build.gradle 、custom/build.gradle 和 根目录/build.gradle 文件中也通过 mPaasBuildType 字段针对不同的接入方式添加了相应配置，自行集成时请留意。

### 降级10.1.60
如需降回10.1.60，请按照如下步骤：

1. 删除mpaas_packages.json 文件。
2. mPaaS IDEA插件 -> 组件管理 -> 选择基线 -> 确认。
3. mPaaS IDEA插件 -> 组件管理 -> 安装扫码组件。
4. 同步工程。

注意：aar 接入方式无法降级10.1.60。