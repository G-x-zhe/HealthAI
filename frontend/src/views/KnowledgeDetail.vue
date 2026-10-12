<template>
  <div class="page-container detail-wrap" v-loading="loading">
    <el-button text :icon="ArrowLeft" class="back-btn" @click="router.back()">返回列表</el-button>

    <template v-if="detail">
      <!-- 头部信息 -->
      <div class="detail-card">
        <div class="detail-head">
          <el-tag :type="tagType(detail.category)" effect="dark" round size="large">{{ detail.category || '未分类' }}</el-tag>
          <el-button
            :type="collected ? 'warning' : 'primary'"
            plain
            round
            class="collect-btn"
            :loading="collectLoading"
            @click="toggleCollect"
          >
            <el-icon style="margin-right: 5px"><Star :fill="collected ? 'currentColor' : 'none'" /></el-icon>
            {{ collected ? '已收藏' : '收藏' }}
          </el-button>
        </div>

        <h1 class="detail-title">{{ detail.title }}</h1>

        <div class="detail-meta">
          <span><el-icon><User /></el-icon>来源：{{ detail.source || '健康百科' }}</span>
          <span><el-icon><View /></el-icon>{{ detail.viewCount || 0 }} 次浏览</span>
          <span><el-icon><Clock /></el-icon>{{ formatTime(detail.createTime) }}</span>
        </div>

        <div v-if="detail.keywords" class="detail-keywords">
          <el-icon><PriceTag /></el-icon>
          <el-tag
            v-for="(kw, i) in keywordList"
            :key="i"
            size="small"
            effect="plain"
            class="kw-tag"
          >{{ kw }}</el-tag>
        </div>

        <el-divider />

        <!-- 正文 -->
        <div class="detail-content">{{ detail.content }}</div>
      </div>

      <!-- 免责提示 -->
      <el-alert
        class="notice"
        type="info"
        :closable="false"
        title="温馨提示"
        description="以上内容仅供健康科普参考，不能作为诊断及治疗依据。如有不适请及时就医。"
        show-icon
      />
    </template>

    <el-empty v-else-if="!loading" description="知识不存在或已下架">
      <el-button type="primary" @click="router.push('/knowledge')">返回知识广场</el-button>
    </el-empty>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, User, View, Clock, PriceTag, Star } from '@element-plus/icons-vue'
import { getKnowledgeDetail } from '../api/knowledge'
import { checkCollected, addCollect, removeCollect } from '../api/collect'
import { tagType, formatTime } from '../utils/category'

const route = useRoute()
const router = useRouter()

const detail = ref(null)
const loading = ref(false)
const collected = ref(false)
const collectLoading = ref(false)

const isLogin = () => !!localStorage.getItem('healthai_token')

const keywordList = computed(() =>
  (detail.value?.keywords || '')
    .split(/[,，、\s]+/)
    .map((s) => s.trim())
    .filter(Boolean)
)

async function loadDetail() {
  loading.value = true
  try {
    const res = await getKnowledgeDetail(route.params.id)
    detail.value = res.data
    if (detail.value && isLogin()) {
      checkCollected(detail.value.knowledgeId)
        .then((r) => (collected.value = !!r.data?.collected))
        .catch(() => {})
    }
  } finally {
    loading.value = false
  }
}

async function toggleCollect() {
  if (!isLogin()) {
    ElMessage.warning('请先登录后再收藏')
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  collectLoading.value = true
  try {
    const id = detail.value.knowledgeId
    if (collected.value) {
      await removeCollect(id)
      collected.value = false
      ElMessage.success('已取消收藏')
    } else {
      await addCollect(id)
      collected.value = true
      ElMessage.success('收藏成功')
    }
  } finally {
    collectLoading.value = false
  }
}

onMounted(loadDetail)
</script>

<style scoped>
.detail-wrap {
  max-width: 860px;
}
.back-btn {
  margin-bottom: 12px;
  font-size: 14px;
}

.detail-card {
  background: #fff;
  border-radius: 16px;
  padding: 32px 36px;
  box-shadow: 0 2px 14px rgba(15, 23, 42, 0.06);
}
.detail-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.collect-btn {
  font-weight: 600;
}
.detail-title {
  margin: 18px 0 14px;
  font-size: 27px;
  line-height: 1.4;
  color: #0f172a;
}
.detail-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 22px;
  font-size: 13.5px;
  color: #64748b;
}
.detail-meta span {
  display: flex;
  align-items: center;
  gap: 5px;
}
.detail-keywords {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 16px;
  color: #94a3b8;
}
.kw-tag {
  border-radius: 999px;
}
.detail-content {
  font-size: 15.5px;
  line-height: 2;
  color: #334155;
  white-space: pre-wrap;
  word-break: break-word;
}
.notice {
  margin-top: 18px;
  border-radius: 12px;
}

@media (max-width: 640px) {
  .detail-card {
    padding: 22px 18px;
  }
  .detail-title {
    font-size: 22px;
  }
}
</style>
