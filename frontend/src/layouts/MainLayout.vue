<template>
  <div class="layout">
    <header class="header">
      <div class="header-inner">
        <div class="brand" @click="router.push('/knowledge')">
          <div class="brand-logo">
            <el-icon><FirstAidKit /></el-icon>
          </div>
          <div class="brand-text">
            <span class="brand-name">HealthAI</span>
            <span class="brand-sub">智慧健康助手</span>
          </div>
        </div>

        <nav class="nav">
          <router-link to="/knowledge" class="nav-item">
            <el-icon><Collection /></el-icon><span>知识广场</span>
          </router-link>
          <router-link to="/chat" class="nav-item">
            <el-icon><ChatDotRound /></el-icon><span>AI 问答</span>
          </router-link>
          <router-link to="/profile" class="nav-item">
            <el-icon><User /></el-icon><span>个人中心</span>
          </router-link>
        </nav>

        <div class="header-right">
          <template v-if="token">
            <el-dropdown @command="onCommand">
              <span class="user-chip">
                <el-avatar :size="30" class="user-avatar">{{ avatarLetter }}</el-avatar>
                <span class="user-name">{{ username || '用户' }}</span>
                <el-icon><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">
                    <el-icon><User /></el-icon>个人中心
                  </el-dropdown-item>
                  <el-dropdown-item command="logout" divided>
                    <el-icon><SwitchButton /></el-icon>退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button text @click="router.push('/login')">登录</el-button>
            <el-button type="primary" round @click="router.push('/register')">注册</el-button>
          </template>
        </div>
      </div>
    </header>

    <main class="main">
      <router-view />
    </main>

    <footer class="footer">HealthAI 智慧健康助手 · 仅供健康科普参考，不能替代专业医疗诊断</footer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { logout, getUserInfo } from '../api/auth'

const router = useRouter()
const route = useRoute()
const token = ref(localStorage.getItem('healthai_token'))
const username = ref('')

const avatarLetter = computed(() => (username.value ? username.value.charAt(0).toUpperCase() : 'U'))

async function loadUser() {
  if (!token.value) return
  try {
    const res = await getUserInfo()
    username.value = res.data?.username || ''
    if (username.value) localStorage.setItem('healthai_user', username.value)
  } catch (e) {
    /* 401 已由拦截器处理 */
  }
}

async function onCommand(command) {
  if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'logout') {
    try {
      await ElMessageBox.confirm('确定退出登录吗？', '提示', {
        confirmButtonText: '退出',
        cancelButtonText: '取消',
        type: 'warning'
      })
    } catch {
      return
    }
    try {
      await logout()
    } catch (e) {
      /* 即使后端失败前端也清除登录态 */
    }
    localStorage.removeItem('healthai_token')
    localStorage.removeItem('healthai_user')
    token.value = ''
    username.value = ''
    router.push('/login')
  }
}

onMounted(loadUser)
// 路由变化时刷新登录态（登录页跳回后 header 立即更新）
watch(
  () => route.fullPath,
  () => {
    token.value = localStorage.getItem('healthai_token')
    if (token.value && !username.value) loadUser()
  }
)
</script>

<style scoped>
.layout {
  display: flex;
  flex-direction: column;
  min-height: 100%;
}

.header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid #eef2f4;
}
.header-inner {
  max-width: 1200px;
  margin: 0 auto;
  height: 64px;
  padding: 0 20px;
  display: flex;
  align-items: center;
  gap: 32px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  flex-shrink: 0;
}
.brand-logo {
  width: 38px;
  height: 38px;
  border-radius: 11px;
  background: linear-gradient(135deg, #14b8a6, #0891b2);
  color: #fff;
  font-size: 21px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(13, 148, 136, 0.35);
}
.brand-text {
  display: flex;
  flex-direction: column;
  line-height: 1.15;
}
.brand-name {
  font-size: 18px;
  font-weight: 700;
  color: #0f766e;
  letter-spacing: 0.5px;
}
.brand-sub {
  font-size: 11px;
  color: #64748b;
}

.nav {
  display: flex;
  gap: 6px;
  flex: 1;
}
.nav-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 10px;
  font-size: 15px;
  color: #475569;
  transition: all 0.2s;
}
.nav-item:hover {
  background: #f0fdfa;
  color: #0d9488;
}
.nav-item.router-active-exact {
  background: #e7f4f3;
  color: #0f766e;
  font-weight: 600;
}

.header-right {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}
.user-chip {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 5px 10px 5px 5px;
  border-radius: 999px;
  cursor: pointer;
  transition: background 0.2s;
}
.user-chip:hover {
  background: #f1f5f9;
}
.user-avatar {
  background: linear-gradient(135deg, #14b8a6, #0891b2);
  font-weight: 600;
  font-size: 14px;
}
.user-name {
  font-size: 14px;
  color: #334155;
}

.main {
  flex: 1;
}

.footer {
  text-align: center;
  padding: 20px;
  font-size: 12px;
  color: #94a3b8;
}

@media (max-width: 768px) {
  .brand-sub,
  .nav-item span,
  .user-name {
    display: none;
  }
  .header-inner {
    gap: 12px;
  }
}
</style>
