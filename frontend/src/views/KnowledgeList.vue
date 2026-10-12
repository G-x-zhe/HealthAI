<template>
  <div class="page-container">
    <!-- 页头 -->
    <div class="page-hero">
      <div>
        <h1>医药知识广场</h1>
        <p>检索权威医药知识，为健康生活保驾护航</p>
      </div>
      <div class="hero-icon"><el-icon><Reading /></el-icon></div>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="keyword"
        placeholder="搜索疾病、症状、药品、检查项目..."
        clearable
        size="large"
        class="kw-input"
        :prefix-icon="Search"
        @keyup.enter="onSearch"
        @clear="onSearch"
      />
      <el-select v-model="category" placeholder="全部分类" clearable size="large" class="cat-select" @change="onSearch">
        <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
      </el-select>
      <el-button type="primary" size="large" :icon="Search" @click="onSearch">搜索</el-button>
      <el-button size="large" :icon="RefreshLeft" @click="onReset">重置</el-button>
    </div>

    <!-- 结果统计 -->
    <div class="result-meta">
      共找到 <b>{{ total }}</b> 条相关知识
    </div>

    <!-- 卡片网格 -->
    <div v-loading="loading" class="card-grid">
      <article
        v-for="item in list"
        :key="item.knowledgeId"
        class="k-card"
        @click="goDetail(item.knowledgeId)"
      >
        <div class="k-card-head">
          <el-tag :type="tagType(item.category)" effect="light" round size="small">{{ item.category || '未分类' }}</el-tag>
          <span class="k-views"><el-icon><View /></el-icon>{{ item.viewCount || 0 }}</span>
        </div>
        <h3 class="k-title clamp-1">{{ item.title }}</h3>
        <p class="k-content clamp-2">{{ stripContent(item.content) }}</p>
        <div class="k-card-foot">
          <span class="k-source clamp-1"><el-icon><Link /></el-icon>{{ item.source || '健康百科' }}</span>
          <span class="k-time">{{ formatTime(item.createTime) }}</span>
        </div>
      </article>
    </div>

    <el-empty v-if="!loading && list.length === 0" description="暂无相关知识，换个关键词试试吧" />

    <!-- 分页 -->
    <div v-if="total > 0" class="pager">
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[9, 12, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        background
        @current-change="loadData"
        @size-change="onSizeChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search, RefreshLeft, View, Link } from '@element-plus/icons-vue'
import { searchKnowledge, getCategories } from '../api/knowledge'
import { tagType, formatTime } from '../utils/category'

const router = useRouter()
const keyword = ref('')
const category = ref('')
const categories = ref([])
const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(9)
const loading = ref(false)

function stripContent(content) {
  if (!content) return '暂无内容简介'
  return content.replace(/[#*`>\-]/g, '').replace(/\n+/g, ' ')
}

async function loadData() {
  loading.value = true
  try {
    const res = await searchKnowledge({
      keyword: keyword.value || undefined,
      category: category.value || undefined,
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    list.value = res.data?.list || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

function onSearch() {
  pageNum.value = 1
  loadData()
}

function onReset() {
  keyword.value = ''
  category.value = ''
  pageNum.value = 1
  loadData()
}

function onSizeChange() {
  pageNum.value = 1
  loadData()
}

function goDetail(id) {
  router.push(`/knowledge/${id}`)
}

onMounted(async () => {
  getCategories()
    .then((res) => (categories.value = res.data || []))
    .catch(() => {})
  loadData()
})
</script>

<style scoped>
.page-hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}
.page-hero h1 {
  margin: 0 0 6px;
  font-size: 26px;
  color: #0f172a;
}
.page-hero p {
  margin: 0;
  color: #64748b;
  font-size: 14px;
}
.hero-icon {
  width: 64px;
  height: 64px;
  border-radius: 18px;
  background: linear-gradient(135deg, #ccfbf1, #cffafe);
  color: #0d9488;
  font-size: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.search-bar {
  background: #fff;
  border-radius: 14px;
  padding: 16px;
  display: flex;
  gap: 12px;
  box-shadow: 0 2px 12px rgba(15, 23, 42, 0.05);
  margin-bottom: 16px;
}
.kw-input {
  flex: 1;
}
.cat-select {
  width: 150px;
  flex-shrink: 0;
}

.result-meta {
  font-size: 13.5px;
  color: #64748b;
  margin: 4px 2px 16px;
}
.result-meta b {
  color: #0d9488;
  font-size: 15px;
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 18px;
  min-height: 120px;
}
.k-card {
  background: #fff;
  border-radius: 14px;
  padding: 20px;
  cursor: pointer;
  border: 1px solid #eef2f5;
  transition: all 0.25s ease;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.k-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 14px 30px rgba(13, 148, 136, 0.14);
  border-color: #99f6e4;
}
.k-card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.k-views {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12.5px;
  color: #94a3b8;
}
.k-title {
  margin: 0;
  font-size: 17px;
  font-weight: 650;
  color: #0f172a;
}
.k-content {
  margin: 0;
  font-size: 13.5px;
  line-height: 1.75;
  color: #64748b;
  flex: 1;
}
.k-card-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding-top: 10px;
  border-top: 1px dashed #eef2f5;
  font-size: 12.5px;
  color: #94a3b8;
}
.k-source {
  display: flex;
  align-items: center;
  gap: 4px;
  max-width: 60%;
}
.k-time {
  flex-shrink: 0;
}

.pager {
  margin-top: 28px;
  display: flex;
  justify-content: center;
}

@media (max-width: 1000px) {
  .card-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
@media (max-width: 640px) {
  .card-grid {
    grid-template-columns: 1fr;
  }
  .search-bar {
    flex-wrap: wrap;
  }
  .cat-select {
    width: calc(50% - 6px);
  }
}
</style>
