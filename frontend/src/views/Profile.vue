<template>
  <div class="page-container">
    <!-- 用户信息卡 -->
    <div class="user-card">
      <el-avatar :size="72" class="big-avatar">{{ letter }}</el-avatar>
      <div class="user-meta">
        <h2>{{ username }}</h2>
        <p><el-icon><CircleCheckFilled /></el-icon>账号状态正常 · 欢迎使用 HealthAI</p>
      </div>
      <el-button type="danger" plain round :icon="SwitchButton" @click="doLogout">退出登录</el-button>
    </div>

    <!-- 两个功能 Tab -->
    <el-tabs v-model="activeTab" class="profile-tabs" @tab-change="onTabChange">
      <!-- 我的收藏 -->
      <el-tab-pane name="collect">
        <template #label>
          <span class="tab-label"><el-icon><Star /></el-icon>我的收藏</span>
        </template>

        <div v-loading="collectLoading" class="collect-grid">
          <div v-for="c in collects" :key="c.collectId" class="collect-card">
            <div class="cc-top">
              <el-tag :type="tagType(c.category)" effect="light" round size="small">{{ c.category || '未分类' }}</el-tag>
              <span class="cc-time">收藏于 {{ formatTime(c.createTime) }}</span>
            </div>
            <h3 class="cc-title clamp-1">{{ c.title }}</h3>
            <div class="cc-actions">
              <el-button text type="primary" :icon="Reading" @click="router.push(`/knowledge/${c.knowledgeId}`)">
                查看详情
              </el-button>
              <el-button text type="danger" :icon="Delete" @click="onRemoveCollect(c.knowledgeId)">取消收藏</el-button>
            </div>
          </div>
        </div>
        <el-empty v-if="!collectLoading && collects.length === 0" description="还没有收藏任何知识">
          <el-button type="primary" @click="router.push('/knowledge')">去知识广场看看</el-button>
        </el-empty>
      </el-tab-pane>

      <!-- 学习提醒 -->
      <el-tab-pane name="remind">
        <template #label>
          <span class="tab-label"><el-icon><Bell /></el-icon>学习提醒</span>
        </template>

        <div class="remind-toolbar">
          <el-button type="primary" round :icon="Plus" @click="openRemindDialog">新建提醒</el-button>
        </div>

        <el-table v-loading="remindLoading" :data="reminds" stripe class="remind-table">
          <el-table-column label="标题" prop="title" min-width="150">
            <template #default="{ row }">
              <span class="rt-title"><el-icon><Notebook /></el-icon>{{ row.title }}</span>
            </template>
          </el-table-column>
          <el-table-column label="内容" prop="content" min-width="200" show-overflow-tooltip />
          <el-table-column label="提醒时间" width="170">
            <template #default="{ row }">
              <span class="rt-time"><el-icon><AlarmClock /></el-icon>{{ formatTime(row.remindTime) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="类型" width="90" align="center">
            <template #default="{ row }">
              <el-tag :type="remindTypeTag(row.remindType)" size="small">{{ remindTypeText(row.remindType) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="90" align="center">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'warning'" size="small" effect="plain">
                {{ row.status === 1 ? '已提醒' : '未提醒' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100" align="center">
            <template #default="{ row }">
              <el-button text type="warning" size="small" @click="onCancelRemind(row.remindId)">取消提醒</el-button>
            </template>
          </el-table-column>
          <template #empty>
            <el-empty description="暂无学习提醒，点击上方按钮创建吧" :image-size="80" />
          </template>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <!-- 新建提醒对话框 -->
    <el-dialog v-model="dialogVisible" title="新建学习提醒" width="460px" destroy-on-close>
      <el-form ref="remindFormRef" :model="remindForm" :rules="remindRules" label-width="84px">
        <el-form-item label="提醒标题" prop="title">
          <el-input v-model="remindForm.title" placeholder="例如：复习感冒章节" maxlength="50" />
        </el-form-item>
        <el-form-item label="备注内容">
          <el-input v-model="remindForm.content" type="textarea" :rows="3" placeholder="提醒自己要做什么（选填）" maxlength="200" />
        </el-form-item>
        <el-form-item label="提醒时间" prop="remindTime">
          <el-date-picker
            v-model="remindForm.remindTime"
            type="datetime"
            placeholder="选择提醒时间"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DD HH:mm:ss"
            class="full-width"
          />
        </el-form-item>
        <el-form-item label="提醒类型">
          <el-radio-group v-model="remindForm.remindType">
            <el-radio :value="0">学习</el-radio>
            <el-radio :value="1">复习</el-radio>
            <el-radio :value="2">考试</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="remindSaving" @click="submitRemind">确认创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Star, Bell, Delete, Reading, Plus, SwitchButton,
  CircleCheckFilled, Notebook, AlarmClock
} from '@element-plus/icons-vue'
import { getMyCollects, removeCollect } from '../api/collect'
import { getMyReminds, createRemind, cancelRemind } from '../api/remind'
import { logout } from '../api/auth'
import { tagType, formatTime } from '../utils/category'

const router = useRouter()
const activeTab = ref('collect')
const username = ref(localStorage.getItem('healthai_user') || '用户')
const letter = username.value.charAt(0).toUpperCase()

/* ===== 收藏 ===== */
const collects = ref([])
const collectLoading = ref(false)

async function loadCollects() {
  collectLoading.value = true
  try {
    const res = await getMyCollects()
    collects.value = res.data || []
  } finally {
    collectLoading.value = false
  }
}

async function onRemoveCollect(knowledgeId) {
  await removeCollect(knowledgeId)
  ElMessage.success('已取消收藏')
  loadCollects()
}

/* ===== 提醒 ===== */
const reminds = ref([])
const remindLoading = ref(false)
const dialogVisible = ref(false)
const remindSaving = ref(false)
const remindFormRef = ref()

const remindForm = reactive({ title: '', content: '', remindTime: '', remindType: 0 })
const remindRules = {
  title: [{ required: true, message: '请输入提醒标题', trigger: 'blur' }],
  remindTime: [{ required: true, message: '请选择提醒时间', trigger: 'change' }]
}

const REMIND_TYPE_MAP = ['学习', '复习', '考试']
const REMIND_TAG_MAP = ['primary', 'warning', 'danger']
function remindTypeText(t) {
  return REMIND_TYPE_MAP[t] ?? '学习'
}
function remindTypeTag(t) {
  return REMIND_TAG_MAP[t] ?? 'primary'
}

async function loadReminds() {
  remindLoading.value = true
  try {
    const res = await getMyReminds()
    reminds.value = res.data || []
  } finally {
    remindLoading.value = false
  }
}

function openRemindDialog() {
  remindForm.title = ''
  remindForm.content = ''
  remindForm.remindTime = ''
  remindForm.remindType = 0
  dialogVisible.value = true
}

async function submitRemind() {
  await remindFormRef.value.validate(async (valid) => {
    if (!valid) return
    remindSaving.value = true
    try {
      await createRemind({
        title: remindForm.title,
        content: remindForm.content || undefined,
        remindTime: remindForm.remindTime,
        remindType: remindForm.remindType
      })
      ElMessage.success('提醒创建成功')
      dialogVisible.value = false
      loadReminds()
    } finally {
      remindSaving.value = false
    }
  })
}

async function onCancelRemind(id) {
  await cancelRemind(id)
  ElMessage.success('提醒已取消')
  loadReminds()
}

function onTabChange(name) {
  if (name === 'collect' && collects.value.length === 0) loadCollects()
  if (name === 'remind' && reminds.value.length === 0) loadReminds()
}

async function doLogout() {
  try {
    await logout()
  } catch (e) {
    /* 忽略 */
  }
  localStorage.removeItem('healthai_token')
  localStorage.removeItem('healthai_user')
  router.push('/login')
}

onMounted(loadCollects)
</script>

<style scoped>
.user-card {
  background: linear-gradient(120deg, #0d9488 0%, #0e7490 100%);
  border-radius: 16px;
  padding: 26px 30px;
  display: flex;
  align-items: center;
  gap: 20px;
  color: #fff;
  box-shadow: 0 10px 28px rgba(13, 148, 136, 0.28);
  margin-bottom: 8px;
}
.big-avatar {
  background: rgba(255, 255, 255, 0.22);
  color: #fff;
  font-size: 28px;
  font-weight: 700;
  flex-shrink: 0;
}
.user-meta {
  flex: 1;
}
.user-meta h2 {
  margin: 0 0 6px;
  font-size: 22px;
}
.user-meta p {
  margin: 0;
  font-size: 13.5px;
  opacity: 0.9;
  display: flex;
  align-items: center;
  gap: 5px;
}

.profile-tabs {
  margin-top: 6px;
  background: #fff;
  border-radius: 14px;
  padding: 8px 22px 24px;
  box-shadow: 0 2px 12px rgba(15, 23, 42, 0.05);
}
.tab-label {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 15px;
}

/* 收藏卡片 */
.collect-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  padding-top: 8px;
}
.collect-card {
  border: 1px solid #eef2f5;
  border-radius: 13px;
  padding: 18px;
  transition: all 0.22s;
}
.collect-card:hover {
  border-color: #99f6e4;
  box-shadow: 0 10px 22px rgba(13, 148, 136, 0.12);
  transform: translateY(-3px);
}
.cc-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}
.cc-time {
  font-size: 12px;
  color: #94a3b8;
}
.cc-title {
  margin: 0 0 12px;
  font-size: 16px;
  color: #0f172a;
}
.cc-actions {
  display: flex;
  justify-content: space-between;
}

/* 提醒 */
.remind-toolbar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 14px;
}
.remind-table {
  width: 100%;
  border-radius: 10px;
}
.rt-title {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-weight: 600;
  color: #334155;
}
.rt-time {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  color: #0f766e;
  font-variant-numeric: tabular-nums;
}
.full-width {
  width: 100%;
}

@media (max-width: 900px) {
  .collect-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
@media (max-width: 600px) {
  .collect-grid {
    grid-template-columns: 1fr;
  }
  .user-card {
    flex-wrap: wrap;
  }
}
</style>
