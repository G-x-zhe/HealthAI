import request from '../utils/request'

// 收藏知识
export function addCollect(knowledgeId) {
  return request.post('/collect/add', null, { params: { knowledgeId } })
}

// 取消收藏
export function removeCollect(knowledgeId) {
  return request.delete('/collect/remove', { params: { knowledgeId } })
}

// 我的收藏列表
export function getMyCollects() {
  return request.get('/collect/my')
}

// 检查是否已收藏，返回 { collected: boolean }
export function checkCollected(knowledgeId) {
  return request.get('/collect/check', { params: { knowledgeId } })
}
