<template>
  <div class="home">
    <h2>改表</h2>
    <p class="lead">上传一份表，改 1 到 3 列，再导出。从下面进入现在会用到的页面。</p>
    <div class="cards">
      <router-link v-for="item in entries" :key="item.path" :to="item.path" class="card">
        <span class="name">{{ item.name }}</span>
        <span class="desc">{{ item.desc }}</span>
      </router-link>
    </div>
  </div>
</template>

<script setup>
const entries = [
  { name: "改表", path: "/tool/sheet", desc: "上传表格，改 1 到 3 列后导出" },
  { name: "开发报表", path: "/tool/report", desc: "用模拟数据看报表" },
  { name: "秒杀", path: "/tool/seckill", desc: "选择商品后抢购或取消" },
  { name: "并发测试", path: "/tool/loadtest", desc: "看同一套库存的并发结果" },
  { name: "订单", path: "/tool/orders", desc: "按商品查看订单" },
  { name: "用户管理", path: "/system/user", desc: "给用户指定角色" },
  { name: "角色管理", path: "/system/role", desc: "给角色勾选能看到的菜单" }
]
</script>

<style scoped>
.home {
  padding: 28px 32px 40px;
}
h2 {
  margin: 0 0 8px;
  font-size: 22px;
  font-weight: 600;
}
.lead {
  margin: 0 0 20px;
  color: #606266;
}
.cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 12px;
  max-width: 980px;
}
.card {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 16px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  background: #fff;
  text-decoration: none;
  color: inherit;
}
.card:hover {
  border-color: #409eff;
}
.name {
  font-size: 16px;
  font-weight: 600;
}
.desc {
  color: #909399;
  font-size: 13px;
  line-height: 1.5;
}
</style>
