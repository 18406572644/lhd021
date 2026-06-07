<template>
  <div class="page-container">
    <div class="page-header">
      <h2>物品归档管理</h2>
      <div class="header-actions">
        <el-button type="warning" @click="handleAutoArchive">
          <el-icon><Clock /></el-icon>执行自动归档
        </el-button>
        <el-button type="danger" :disabled="!selectedItems.length" @click="handleBatchArchive">
          <el-icon><Delete /></el-icon>批量归档
        </el-button>
      </div>
    </div>

    <el-alert
      title="归档规则说明"
      type="info"
      :closable="false"
      style="margin-bottom: 20px;"
    >
      <template #default>
        <ul style="margin: 0; padding-left: 20px;">
          <li>自动归档：系统每日扫描，已下架超过30天且无任何操作的物品自动归档</li>
          <li>手动归档：管理员可手动将已下架物品归档</li>
          <li>归档后物品不可直接恢复，如需恢复请联系管理员</li>
          <li>归档数据永久保留，可用于统计分析</li>
        </ul>
      </template>
    </el-alert>

    <el-tabs v-model="activeTab">
      <el-tab-pane label="待归档" name="pending">
        <div class="page-card">
          <div class="search-form">
            <el-form :inline="true" :model="queryForm">
              <el-form-item label="关键词">
                <el-input v-model="queryForm.keyword" placeholder="物品名称" clearable style="width: 180px" />
              </el-form-item>
              <el-form-item label="下架天数">
                <el-select v-model="queryForm.offlineDays" placeholder="全部" clearable style="width: 140px">
                  <el-option label="超过7天" :value="7" />
                  <el-option label="超过15天" :value="15" />
                  <el-option label="超过30天" :value="30" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="loadPendingItems">查询</el-button>
                <el-button @click="resetQuery">重置</el-button>
              </el-form-item>
            </el-form>
          </div>

          <el-table :data="pendingItems" v-loading="loading" @selection-change="handleSelectionChange">
            <el-table-column type="selection" width="50" />
            <el-table-column prop="id" label="ID" width="70" />
            <el-table-column prop="name" label="物品名称" min-width="180" />
            <el-table-column prop="category" label="分类" width="100" />
            <el-table-column prop="itemCondition" label="成色" width="90" />
            <el-table-column prop="quantity" label="数量" width="80" />
            <el-table-column prop="publisherName" label="发布人" width="100" />
            <el-table-column prop="offlineTime" label="下架时间" width="160" />
            <el-table-column label="下架天数" width="100">
              <template #default="{ row }">
                <el-tag size="small" :type="getOfflineDaysTag(row.offlineDays)">{{ row.offlineDays }}天</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="handleView(row)">查看</el-button>
                <el-button link type="danger" @click="handleArchive(row)">归档</el-button>
                <el-button link type="success" @click="handleRestore(row)">恢复上架</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-wrapper">
            <el-pagination
              v-model:current-page="queryForm.pageNum"
              v-model:page-size="queryForm.pageSize"
              :total="pendingTotal"
              :page-sizes="[10, 20, 50]"
              layout="total, sizes, prev, pager, next, jumper"
              @current-change="loadPendingItems"
              @size-change="handleSizeChange"
            />
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="已归档" name="archived">
        <div class="page-card">
          <el-table :data="archivedItems" v-loading="loading">
            <el-table-column prop="id" label="归档ID" width="90" />
            <el-table-column prop="itemName" label="物品名称" min-width="180" />
            <el-table-column prop="category" label="分类" width="100" />
            <el-table-column prop="itemCondition" label="成色" width="90" />
            <el-table-column prop="quantity" label="数量" width="80" />
            <el-table-column prop="archiveType" label="归档类型" width="100">
              <template #default="{ row }">
                <el-tag size="small" :type="row.archiveType === '自动' ? 'info' : 'warning'">{{ row.archiveType }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="archiveReason" label="归档原因" min-width="150" show-overflow-tooltip />
            <el-table-column prop="archiveTime" label="归档时间" width="160" />
            <el-table-column prop="operatorName" label="操作人" width="100" />
          </el-table>
          <div class="pagination-wrapper">
            <el-pagination
              v-model:current-page="archiveQuery.pageNum"
              v-model:page-size="archiveQuery.pageSize"
              :total="archiveTotal"
              :page-sizes="[10, 20, 50]"
              layout="total, sizes, prev, pager, next, jumper"
              @current-change="loadArchivedItems"
              @size-change="handleArchiveSizeChange"
            />
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="detailVisible" title="物品详情" width="600px">
      <el-descriptions :column="2" border v-if="currentItem">
        <el-descriptions-item label="物品名称">{{ currentItem.name }}</el-descriptions-item>
        <el-descriptions-item label="分类">{{ currentItem.category }}</el-descriptions-item>
        <el-descriptions-item label="成色">{{ currentItem.itemCondition }}</el-descriptions-item>
        <el-descriptions-item label="数量">{{ currentItem.quantity }}</el-descriptions-item>
        <el-descriptions-item label="发布人">{{ currentItem.publisherName }}</el-descriptions-item>
        <el-descriptions-item label="自提点">{{ currentItem.pickupPointName }}</el-descriptions-item>
        <el-descriptions-item label="发布时间">{{ currentItem.createTime }}</el-descriptions-item>
        <el-descriptions-item label="下架时间">{{ currentItem.offlineTime }}</el-descriptions-item>
        <el-descriptions-item label="下架天数">{{ currentItem.offlineDays }}天</el-descriptions-item>
        <el-descriptions-item label="浏览量">{{ currentItem.viewCount }}</el-descriptions-item>
        <el-descriptions-item label="互换次数">{{ currentItem.exchangeCount }}</el-descriptions-item>
        <el-descriptions-item label="物品描述" :span="2">{{ currentItem.description }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { api } from '@/api'

const loading = ref(false)
const activeTab = ref('pending')
const selectedItems = ref([])
const detailVisible = ref(false)
const currentItem = ref(null)

const pendingItems = ref([])
const pendingTotal = ref(0)
const archivedItems = ref([])
const archiveTotal = ref(0)

const queryForm = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  offlineDays: null
})

const archiveQuery = reactive({
  pageNum: 1,
  pageSize: 10
})

const getOfflineDaysTag = (days) => {
  if (days >= 30) return 'danger'
  if (days >= 15) return 'warning'
  return 'info'
}

const loadPendingItems = async () => {
  loading.value = true
  try {
    const res = await api.itemArchive.getPendingArchive(queryForm)
    pendingItems.value = res.list
    pendingTotal.value = res.total
  } catch (e) {
    console.error(e)
    pendingItems.value = [
      { id: 1, name: '旧书籍一批', category: '书籍', itemCondition: '八成新', quantity: 5, publisherName: '张三', offlineTime: '2024-01-01 10:00:00', offlineDays: 45, createTime: '2023-12-01', description: '各种类型旧书籍' }
    ]
    pendingTotal.value = 1
  } finally {
    loading.value = false
  }
}

const loadArchivedItems = async () => {
  loading.value = true
  try {
    const res = await api.itemArchive.page(archiveQuery)
    archivedItems.value = res.list
    archiveTotal.value = res.total
  } catch (e) {
    console.error(e)
    archivedItems.value = [
      { id: 1, itemName: '旧衣物', category: '衣物', itemCondition: '七成新', quantity: 10, archiveType: '自动', archiveReason: '下架超过30天', archiveTime: '2024-01-15 02:00:00', operatorName: '系统' }
    ]
    archiveTotal.value = 1
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  Object.assign(queryForm, { pageNum: 1, pageSize: 10, keyword: '', offlineDays: null })
  loadPendingItems()
}

const handleSizeChange = () => {
  queryForm.pageNum = 1
  loadPendingItems()
}

const handleArchiveSizeChange = () => {
  archiveQuery.pageNum = 1
  loadArchivedItems()
}

const handleSelectionChange = (val) => {
  selectedItems.value = val
}

const handleView = (row) => {
  currentItem.value = row
  detailVisible.value = true
}

const handleArchive = async (row) => {
  ElMessageBox.confirm(`确定归档物品「${row.name}」吗？`, '提示', { type: 'warning' }).then(async () => {
    await api.itemArchive.archive(row.id)
    ElMessage.success('归档成功')
    loadPendingItems()
  }).catch(() => {})
}

const handleBatchArchive = async () => {
  const ids = selectedItems.value.map(item => item.id)
  ElMessageBox.confirm(`确定归档选中的 ${ids.length} 件物品吗？`, '提示', { type: 'warning' }).then(async () => {
    await api.itemArchive.batchArchive(ids)
    ElMessage.success('批量归档成功')
    loadPendingItems()
  }).catch(() => {})
}

const handleRestore = async (row) => {
  ElMessageBox.confirm(`确定恢复物品「${row.name}」吗？恢复后将重新上架。`, '提示', { type: 'warning' }).then(async () => {
    await api.itemArchive.restore(row.id)
    ElMessage.success('恢复成功')
    loadPendingItems()
  }).catch(() => {})
}

const handleAutoArchive = async () => {
  ElMessageBox.confirm('确定执行自动归档吗？系统将自动归档下架超过30天的物品。', '提示', { type: 'warning' }).then(async () => {
    const res = await api.itemArchive.autoArchive()
    ElMessage.success(`自动归档完成，共归档 ${res} 件物品`)
    loadPendingItems()
  }).catch(() => {})
}

watch(activeTab, (val) => {
  if (val === 'archived') {
    loadArchivedItems()
  }
})

onMounted(() => {
  loadPendingItems()
})
</script>
