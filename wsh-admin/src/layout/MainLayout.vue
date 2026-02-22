<template>
  <el-container class="main-layout">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '220px'" class="aside">
      <div class="logo" :class="{ collapsed: isCollapse }">
        <span v-if="!isCollapse">微生活券吧</span>
        <span v-else>WSH</span>
      </div>
      <el-menu
        :default-active="currentRoute"
        :collapse="isCollapse"
        router
        background-color="#001529"
        text-color="#ffffffb3"
        active-text-color="#409eff"
      >
        <template v-for="item in menuItems" :key="item.path">
          <el-menu-item :index="item.path">
            <el-icon><component :is="item.icon" /></el-icon>
            <template #title>{{ item.title }}</template>
          </el-menu-item>
        </template>
      </el-menu>
    </el-aside>

    <el-container>
      <!-- 顶栏 -->
      <el-header class="header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="isCollapse = !isCollapse">
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item>平台运营后台</el-breadcrumb-item>
            <el-breadcrumb-item v-if="currentTitle">{{ currentTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-icon><UserFilled /></el-icon>
              {{ authStore.realName || authStore.username }}
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 内容区 -->
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const isCollapse = ref(false)

const menuItems = [
  { path: '/dashboard', title: '仪表盘', icon: 'DataBoard' },
  { path: '/merchants', title: '商户管理', icon: 'Shop' },
  { path: '/users', title: '用户管理', icon: 'User' },
  { path: '/activities', title: '活动管理', icon: 'Present' },
  { path: '/billing', title: '费用管理', icon: 'Money' },
  { path: '/logs', title: '操作日志', icon: 'Document' }
]

const currentRoute = computed(() => '/' + route.path.split('/')[1])
const currentTitle = computed(() => {
  const item = menuItems.find(m => m.path === currentRoute.value)
  return item?.title || (route.meta?.title as string) || ''
})

function handleCommand(command: string) {
  if (command === 'logout') {
    authStore.logout()
    router.push('/login')
  }
}
</script>

<style scoped>
.main-layout {
  height: 100vh;
}
.aside {
  background-color: #001529;
  transition: width 0.3s;
  overflow: hidden;
}
.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
  border-bottom: 1px solid #ffffff1a;
  white-space: nowrap;
}
.logo.collapsed {
  font-size: 14px;
}
.el-menu {
  border-right: none;
}
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  padding: 0 20px;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}
.collapse-btn {
  font-size: 20px;
  cursor: pointer;
  color: #606266;
}
.collapse-btn:hover {
  color: #409eff;
}
.header-right {
  display: flex;
  align-items: center;
}
.user-info {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  color: #606266;
  font-size: 14px;
}
.main-content {
  background: #f0f2f5;
  padding: 20px;
}
</style>
