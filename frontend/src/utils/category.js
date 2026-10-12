// 知识分类对应的 Element Plus tag 类型
const CATEGORY_TAG_TYPE = {
  药品: 'primary',
  疾病: 'danger',
  症状: 'warning',
  检查: 'success',
  广告法规: 'info'
}

export function tagType(category) {
  return CATEGORY_TAG_TYPE[category] || 'info'
}

// 日期时间统一显示为 yyyy-MM-dd HH:mm
export function formatTime(value) {
  if (!value) return ''
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return String(value).replace('T', ' ').slice(0, 16)
  const p = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}`
}
