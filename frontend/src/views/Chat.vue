<template>
  <div class="chat-page">
    <!-- 左侧会话栏 -->
    <aside class="chat-sidebar">
      <el-button type="primary" class="new-chat-btn" :icon="EditPen" @click="newSession">新对话</el-button>

      <div class="sidebar-title">历史会话</div>
      <div v-loading="sessionsLoading" class="session-list">
        <div
          v-for="s in sessions"
          :key="s.sessionId"
          class="session-item"
          :class="{ active: s.sessionId === activeSessionId }"
          @click="switchSession(s.sessionId)"
        >
          <el-icon class="s-icon"><ChatLineSquare /></el-icon>
          <div class="s-info">
            <div class="s-title clamp-1">{{ s.title }}</div>
            <div class="s-time">{{ formatTime(s.time) }}</div>
          </div>
          <el-popconfirm title="确定删除该会话吗？" width="210" confirm-button-text="删除" cancel-button-text="取消" @confirm="onDelete(s.sessionId)">
            <template #reference>
              <el-icon class="s-del" @click.stop><Delete /></el-icon>
            </template>
          </el-popconfirm>
        </div>
        <el-empty v-if="!sessionsLoading && sessions.length === 0" description="还没有历史会话" :image-size="70" />
      </div>
    </aside>

    <!-- 右侧对话区 -->
    <section class="chat-main">
      <div class="chat-header">
        <el-icon class="header-ai"><ChatDotRound /></el-icon>
        <div>
          <div class="header-title">AI 健康问答</div>
          <div class="header-sub">DeepSeek 模型 · 医学知识增强 · 保留最近 10 轮上下文</div>
        </div>
      </div>

      <div ref="msgBoxRef" class="msg-box">
        <!-- 欢迎态 -->
        <div v-if="messages.length === 0 && !asking" class="welcome">
          <div class="welcome-logo"><el-icon><FirstAidKit /></el-icon></div>
          <h2>您好，我是 HealthAI 健康助手</h2>
          <p>您可以向我咨询疾病、症状、用药等健康问题</p>
          <div class="quick-list">
            <div v-for="q in quickQuestions" :key="q" class="quick-item" @click="quickAsk(q)">
              <el-icon><ChatLineRound /></el-icon>{{ q }}
            </div>
          </div>
        </div>

        <!-- 消息流 -->
        <template v-for="(m, i) in messages" :key="i">
          <div v-if="m.role === 'user'" class="msg-row user msg-in">
            <div class="bubble user-bubble">{{ m.content }}</div>
            <el-avatar :size="36" class="avatar user-avatar">{{ userLetter }}</el-avatar>
          </div>
          <div v-else class="msg-row ai msg-in">
            <el-avatar :size="36" class="avatar ai-avatar"><el-icon><Service /></el-icon></el-avatar>
            <div class="bubble ai-bubble">
              <div class="bubble-meta">
                <el-tag size="small" type="success" effect="plain">{{ m.modelName || 'deepseek-r1:1.5b' }}</el-tag>
                <el-tag v-if="m.isCached" size="small" type="warning" effect="plain">缓存命中</el-tag>
              </div>
              <div class="bubble-text">{{ m.content }}</div>
            </div>
          </div>
        </template>

        <!-- 加载态 -->
        <div v-if="asking" class="msg-row ai msg-in">
          <el-avatar :size="36" class="avatar ai-avatar"><el-icon><Service /></el-icon></el-avatar>
          <div class="bubble ai-bubble typing">
            <span></span><span></span><span></span>
            <span class="typing-text">AI 正在思考中，请稍候…</span>
          </div>
        </div>
      </div>

      <!-- 输入区 -->
      <div class="input-area">
        <el-input
          v-model="question"
          type="textarea"
          :rows="2"
          resize="none"
          placeholder="请输入您的健康问题，Enter 发送，Shift+Enter 换行"
          @keydown.enter.exact.prevent="send"
        />
        <el-button type="primary" circle class="send-btn" :loading="asking" :disabled="!question.trim()" @click="send">
          <el-icon v-if="!asking"><Promotion /></el-icon>
        </el-button>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  EditPen, Delete, ChatDotRound, ChatLineSquare, ChatLineRound,
  Promotion, Service, FirstAidKit
} from '@element-plus/icons-vue'
import { askQuestion, getHistory, getSessionHistory, deleteSession } from '../api/chat'
import { formatTime } from '../utils/category'

const route = useRoute()
const router = useRouter()

const quickQuestions = ['感冒了吃什么好得快？', '一直头痛可能是什么原因？', '布洛芬有哪些注意事项？']

const sessions = ref([])
const sessionsLoading = ref(false)
const activeSessionId = ref(null)
const messages = ref([])
const question = ref('')
const asking = ref(false)
const msgBoxRef = ref()
const userLetter = (localStorage.getItem('healthai_user') || '我').charAt(0)

function scrollBottom() {
  nextTick(() => {
    const box = msgBoxRef.value
    if (box) box.scrollTop = box.scrollHeight
  })
}

// 加载历史并按会话分组
async function loadSessions() {
  sessionsLoading.value = true
  try {
    const res = await getHistory({ pageNum: 1, pageSize: 100 })
    const records = res.data?.list || []
    const map = new Map()
    for (const r of records) {
      if (!map.has(r.sessionId)) {
        map.set(r.sessionId, {
          sessionId: r.sessionId,
          title: '新会话',
          time: r.createTime
        })
      }
      const g = map.get(r.sessionId)
      // records 按时间倒序，遍历到的最后一条提问就是该会话的第一个问题，用它做标题
      if (r.question) g.title = r.question
      if (new Date(r.createTime) > new Date(g.time)) g.time = r.createTime
    }
    sessions.value = Array.from(map.values()).sort((a, b) => new Date(b.time) - new Date(a.time))
  } finally {
    sessionsLoading.value = false
  }
}

// 当前会话同步到 URL，刷新页面后可自动恢复（显式带 path，避免相对导航不更新 URL）
function syncUrl(sessionId) {
  router.replace({ path: '/chat', query: sessionId ? { sessionId } : {} })
}

async function switchSession(sessionId) {
  activeSessionId.value = sessionId
  syncUrl(sessionId)
  const res = await getSessionHistory(sessionId, { pageNum: 1, pageSize: 100 })
  // 历史表每行是一轮问答（question/answer 两列），展开为用户、AI 两条气泡
  const msgs = []
  for (const r of res.data?.list || []) {
    msgs.push({ role: 'user', content: r.question })
    msgs.push({
      role: 'assistant',
      content: r.answer,
      modelName: r.modelName,
      isCached: r.isCached === 1 || r.isCached === true
    })
  }
  messages.value = msgs
  scrollBottom()
}

function newSession() {
  activeSessionId.value = null
  messages.value = []
  question.value = ''
  syncUrl(null)
}

async function onDelete(sessionId) {
  await deleteSession(sessionId)
  ElMessage.success('会话已删除')
  if (activeSessionId.value === sessionId) newSession()
  loadSessions()
}

async function doAsk(q) {
  if (asking.value) return
  let sessionId = activeSessionId.value
  if (!sessionId) {
    sessionId = 'web-' + Date.now()
    activeSessionId.value = sessionId
    syncUrl(sessionId)
  }
  messages.value.push({ role: 'user', content: q })
  asking.value = true
  scrollBottom()
  try {
    const res = await askQuestion({ question: q, sessionId })
    // 以后端最终确认的 sessionId 为准（后端可能自行生成）
    if (res.data?.sessionId) {
      activeSessionId.value = res.data.sessionId
      syncUrl(res.data.sessionId)
    }
    messages.value.push({
      role: 'assistant',
      content: res.data?.answer || '（暂无回复内容）',
      modelName: res.data?.modelName,
      isCached: res.data?.isCached
    })
    loadSessions()
  } catch (e) {
    messages.value.push({
      role: 'assistant',
      content: '抱歉，回答出错了，请确认 AI 服务（Ollama）已启动后重试。'
    })
  } finally {
    asking.value = false
    scrollBottom()
  }
}

function send() {
  const q = question.value.trim()
  if (!q) return
  question.value = ''
  doAsk(q)
}

function quickAsk(q) {
  doAsk(q)
}

onMounted(async () => {
  await loadSessions()
  // 刷新/分享链接恢复：URL 带 sessionId 且属于当前用户时，自动打开该会话
  const sid = route.query.sessionId
  if (sid && sessions.value.some((s) => s.sessionId === sid)) {
    switchSession(sid)
  }
})
</script>

<style scoped>
.chat-page {
  display: flex;
  gap: 16px;
  padding: 16px 20px;
  max-width: 1200px;
  margin: 0 auto;
  height: calc(100vh - 128px);
  min-height: 480px;
}

/* 侧栏 */
.chat-sidebar {
  width: 264px;
  flex-shrink: 0;
  background: #fff;
  border-radius: 14px;
  padding: 14px;
  display: flex;
  flex-direction: column;
  box-shadow: 0 2px 12px rgba(15, 23, 42, 0.05);
}
.new-chat-btn {
  width: 100%;
  height: 42px;
  font-size: 15px;
  border-radius: 10px;
}
.sidebar-title {
  font-size: 12.5px;
  color: #94a3b8;
  margin: 18px 4px 10px;
  letter-spacing: 1px;
}
.session-list {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.session-item {
  display: flex;
  align-items: center;
  gap: 9px;
  padding: 10px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.18s;
}
.session-item:hover {
  background: #f0fdfa;
}
.session-item.active {
  background: #e7f4f3;
}
.s-icon {
  color: #0d9488;
  font-size: 16px;
  flex-shrink: 0;
}
.s-info {
  flex: 1;
  min-width: 0;
}
.s-title {
  font-size: 13.5px;
  color: #334155;
}
.session-item.active .s-title {
  color: #0f766e;
  font-weight: 600;
}
.s-time {
  font-size: 11.5px;
  color: #94a3b8;
  margin-top: 2px;
}
.s-del {
  color: #cbd5e1;
  font-size: 15px;
  flex-shrink: 0;
}
.s-del:hover {
  color: #ef4444;
}

/* 主区 */
.chat-main {
  flex: 1;
  min-width: 0;
  background: #fff;
  border-radius: 14px;
  display: flex;
  flex-direction: column;
  box-shadow: 0 2px 12px rgba(15, 23, 42, 0.05);
  overflow: hidden;
}
.chat-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 22px;
  border-bottom: 1px solid #f1f5f9;
}
.header-ai {
  width: 40px;
  height: 40px;
  border-radius: 11px;
  background: linear-gradient(135deg, #14b8a6, #0891b2);
  color: #fff;
  font-size: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.header-title {
  font-size: 16px;
  font-weight: 650;
  color: #0f172a;
}
.header-sub {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 2px;
}

.msg-box {
  flex: 1;
  overflow-y: auto;
  padding: 24px 26px;
  background: linear-gradient(180deg, #fbfefd 0%, #f6faf9 100%);
}

/* 欢迎态 */
.welcome {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  gap: 10px;
}
.welcome-logo {
  width: 72px;
  height: 72px;
  border-radius: 22px;
  background: linear-gradient(135deg, #14b8a6, #0891b2);
  color: #fff;
  font-size: 38px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 10px 26px rgba(13, 148, 136, 0.35);
  margin-bottom: 8px;
}
.welcome h2 {
  margin: 0;
  font-size: 21px;
  color: #0f172a;
}
.welcome p {
  margin: 0 0 18px;
  color: #94a3b8;
  font-size: 14px;
}
.quick-list {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  justify-content: center;
}
.quick-item {
  display: flex;
  align-items: center;
  gap: 7px;
  padding: 10px 18px;
  background: #fff;
  border: 1px solid #d5efeb;
  border-radius: 999px;
  font-size: 13.5px;
  color: #0f766e;
  cursor: pointer;
  transition: all 0.2s;
}
.quick-item:hover {
  background: #e7f4f3;
  border-color: #5eead4;
  transform: translateY(-2px);
}

/* 消息行 */
.msg-row {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  align-items: flex-start;
}
.msg-row.user {
  justify-content: flex-end;
}
.avatar {
  flex-shrink: 0;
}
.user-avatar {
  background: linear-gradient(135deg, #0d9488, #0e7490);
  color: #fff;
  font-size: 14px;
  font-weight: 600;
}
.ai-avatar {
  background: #fff;
  border: 1.5px solid #5eead4;
  color: #0d9488;
}
.bubble {
  max-width: 74%;
  padding: 12px 16px;
  border-radius: 14px;
  font-size: 14.5px;
  line-height: 1.8;
}
.user-bubble {
  background: linear-gradient(135deg, #14b8a6, #0891b2);
  color: #fff;
  border-bottom-right-radius: 4px;
  white-space: pre-wrap;
  word-break: break-word;
}
.ai-bubble {
  background: #fff;
  border: 1px solid #e8f3f1;
  border-bottom-left-radius: 4px;
  color: #334155;
  box-shadow: 0 2px 8px rgba(15, 23, 42, 0.04);
}
.bubble-meta {
  display: flex;
  gap: 6px;
  margin-bottom: 7px;
}
.bubble-text {
  white-space: pre-wrap;
  word-break: break-word;
}

/* 打字动画 */
.typing {
  display: flex;
  align-items: center;
  gap: 5px;
}
.typing span:not(.typing-text) {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #14b8a6;
  animation: blink 1.3s infinite ease-in-out;
}
.typing span:nth-child(2) {
  animation-delay: 0.2s;
}
.typing span:nth-child(3) {
  animation-delay: 0.4s;
}
.typing-text {
  margin-left: 6px;
  font-size: 13px;
  color: #94a3b8;
}
@keyframes blink {
  0%, 80%, 100% { opacity: 0.25; }
  40% { opacity: 1; }
}

/* 输入区 */
.input-area {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  padding: 14px 20px;
  border-top: 1px solid #f1f5f9;
}
.input-area :deep(.el-textarea__inner) {
  border-radius: 12px;
  padding: 10px 14px;
  font-size: 14.5px;
}
.send-btn {
  width: 44px;
  height: 44px;
  flex-shrink: 0;
  background: linear-gradient(135deg, #14b8a6, #0891b2);
  border: none;
  font-size: 18px;
}
.send-btn:hover {
  opacity: 0.9;
}

@media (max-width: 768px) {
  .chat-page {
    padding: 10px;
    height: calc(100vh - 118px);
  }
  .chat-sidebar {
    display: none;
  }
  .bubble {
    max-width: 85%;
  }
}
</style>
