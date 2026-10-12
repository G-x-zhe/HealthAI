<template>
  <div class="auth-page">
    <div class="auth-card">
      <!-- 左侧品牌区 -->
      <div class="auth-brand">
        <div class="brand-top">
          <div class="brand-logo"><el-icon><FirstAidKit /></el-icon></div>
          <span class="brand-title">HealthAI</span>
        </div>
        <h1 class="brand-slogan">您的 24 小时<br />智慧健康顾问</h1>
        <p class="brand-desc">基于大语言模型与专业医药知识库，<br />提供问答、知识检索与个性化学习提醒服务。</p>
        <ul class="brand-points">
          <li><el-icon><ChatDotRound /></el-icon>AI 智能问答，上下文连续对话</li>
          <li><el-icon><Collection /></el-icon>权威医药知识，分类检索收藏</li>
          <li><el-icon><Bell /></el-icon>学习提醒，健康知识温故知新</li>
        </ul>
        <div class="brand-glow"></div>
      </div>

      <!-- 右侧表单区 -->
      <div class="auth-form-side">
        <div class="auth-form">
          <h2>欢迎回来</h2>
          <p class="form-tip">登录后开启您的智慧健康之旅</p>

          <el-form ref="formRef" :model="form" :rules="rules" size="large" @submit.prevent="onSubmit">
            <el-form-item prop="username">
              <el-input v-model="form.username" placeholder="请输入用户名" :prefix-icon="User" size="large" />
            </el-form-item>
            <el-form-item prop="password">
              <el-input
                v-model="form.password"
                type="password"
                placeholder="请输入密码"
                :prefix-icon="Lock"
                show-password
                size="large"
                @keyup.enter="onSubmit"
              />
            </el-form-item>
            <el-button type="primary" size="large" class="submit-btn" :loading="loading" @click="onSubmit">
              登 录
            </el-button>
          </el-form>

          <div class="form-footer">
            还没有账号？<router-link to="/register" class="link">立即注册</router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { login } from '../api/auth'

const router = useRouter()
const route = useRoute()
const formRef = ref()
const loading = ref(false)

const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function onSubmit() {
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const res = await login(form)
      localStorage.setItem('healthai_token', res.data)
      ElMessage.success('登录成功')
      router.push(route.query.redirect || '/knowledge')
    } catch (e) {
      /* 错误提示已由拦截器统一处理 */
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.auth-page {
  min-height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: linear-gradient(135deg, #e0f4f2 0%, #f0fdfa 45%, #ecfeff 100%);
}
.auth-card {
  width: 900px;
  max-width: 100%;
  min-height: 540px;
  background: #fff;
  border-radius: 22px;
  overflow: hidden;
  display: flex;
  box-shadow: 0 24px 60px rgba(13, 148, 136, 0.18);
}

/* 品牌区 */
.auth-brand {
  position: relative;
  width: 46%;
  padding: 40px 36px;
  background: linear-gradient(155deg, #0d9488 0%, #0e7490 100%);
  color: #fff;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.brand-top {
  display: flex;
  align-items: center;
  gap: 10px;
}
.brand-logo {
  width: 38px;
  height: 38px;
  border-radius: 11px;
  background: rgba(255, 255, 255, 0.18);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 21px;
}
.brand-title {
  font-size: 20px;
  font-weight: 700;
  letter-spacing: 1px;
}
.brand-slogan {
  margin: 56px 0 14px;
  font-size: 27px;
  font-weight: 700;
  line-height: 1.45;
}
.brand-desc {
  margin: 0;
  font-size: 13.5px;
  line-height: 1.9;
  opacity: 0.85;
}
.brand-points {
  margin: 36px 0 0;
  padding: 0;
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 16px;
  font-size: 13.5px;
}
.brand-points li {
  display: flex;
  align-items: center;
  gap: 10px;
  opacity: 0.92;
}
.brand-points .el-icon {
  font-size: 17px;
  background: rgba(255, 255, 255, 0.16);
  border-radius: 50%;
  padding: 5px;
}
.brand-glow {
  position: absolute;
  right: -80px;
  bottom: -80px;
  width: 260px;
  height: 260px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(103, 232, 249, 0.35), transparent 70%);
}

/* 表单区 */
.auth-form-side {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 48px;
}
.auth-form {
  width: 100%;
  max-width: 340px;
}
.auth-form h2 {
  margin: 0 0 8px;
  font-size: 25px;
  color: #0f172a;
}
.form-tip {
  margin: 0 0 30px;
  font-size: 14px;
  color: #94a3b8;
}
.submit-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  letter-spacing: 4px;
  border-radius: 10px;
}
.form-footer {
  margin-top: 22px;
  text-align: center;
  font-size: 14px;
  color: #64748b;
}
.link {
  color: #0d9488;
  font-weight: 600;
}
.link:hover {
  text-decoration: underline;
}

@media (max-width: 760px) {
  .auth-brand {
    display: none;
  }
  .auth-form-side {
    padding: 40px 28px;
  }
}
</style>
