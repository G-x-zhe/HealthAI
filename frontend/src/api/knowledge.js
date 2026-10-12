import request from '../utils/request'

// 分页搜索医药知识 { keyword, category, pageNum, pageSize }
export function searchKnowledge(params) {
  return request.get('/knowledge/search', { params })
}

// 查看知识详情（浏览量 +1）
export function getKnowledgeDetail(knowledgeId) {
  return request.get(`/knowledge/detail/${knowledgeId}`)
}

// 获取知识分类列表
export function getCategories() {
  return request.get('/knowledge/categories')
}
