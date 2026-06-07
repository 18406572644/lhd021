<template>
  <div class="page-container">
    <div class="page-header">
      <h2>闲置物品管理</h2>
      <div class="header-actions">
        <el-button type="primary" @click="router.push('/idle-item/publish')">
          <el-icon><Plus /></el-icon>发布物品
        </el-button>
        <el-button type="warning" :disabled="!selectedItems.length" @click="handleBatchOffline">
          <el-icon><Bottom /></el-icon>批量下架
        </el-button>
      </div>
    </div>

    <div class="page-card">
      <div class="search-form">
        <el-form :inline="true" :model="queryForm">
          <el-form-item label="关键词">
            <el-input v-model="queryForm.keyword" placeholder="物品名称/描述" clearable style="width: 200px" />
          </el-form-item>
          <el-form-item label="分类">
            <el-select v-model="queryForm.category" placeholder="全部分类" clearable style="width: 140px">
              <el-option v-for="cat in categories" :key="cat.value" :label="cat.label" :value="cat.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="queryForm.status" placeholder="全部状态" clearable style="width: 140px">
              <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="成色">
            <el-select v-model="queryForm.condition" placeholder="全部成色" clearable style="width: 140px">
              <el-option v-for="c in conditionOptions" :key="c.value" :label="c.label" :value="c.value" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadData">查询</el-button>
            <el-button @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table :data="tableData" v-loading="loading" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" />
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="name" label="物品名称" min-width="160">
          <template #default="{ row }">
            <div class="item-name">
              <div class="item-thumb" v-if="row.imageUrl">
                <img :src="row.imageUrl" alt="" />
              </div>
              <div class="item-text">
                <div class="name">{{ row.name }}</div>
                <div class="desc">{{ row.description }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="category" label="分类" width="100" />
        <el-table-column prop="itemCondition" label="成色" width="90">
          <template #default="{ row }">
            <el-tag size="small" :type="getConditionTag(row.itemCondition)">{{ row.itemCondition }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="80" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="getStatusTag(row.status)">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="viewCount" label="浏览" width="70" />
        <el-table-column prop="exchangeCount" label="互换" width="70" />
        <el-table-column prop="publisherName" label="发布人" width="100" />
        <el-table-column prop="createTime" label="发布时间" width="160" />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">查看</el-button>
            <el-button link type="primary" @click="handleExchange(row)" v-if="row.status === '可互换'">申请互换</el-button>
            <el-button link type="warning" @click="handleOffline(row)" v-if="row.status === '可互换'">下架</el-button>
            <el-button link type="danger" @click="handleArchive(row)" v-if="row.status === '已下架'">归档</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="queryForm.pageNum"
          v-model:page-size="queryForm.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="loadData"
          @size-change="handleSizeChange"
        />
      </div>
    </div>

    <el-dialog v-model="detailVisible" title="物品详情" width="600px">
      <el-descriptions :column="2" border v-if="currentItem">
        <el-descriptions-item label="物品名称">{{ currentItem.name }}</el-descriptions-item>
        <el-descriptions-item label="分类">{{ currentItem.category }}</el-descriptions-item>
        <el-descriptions-item label="成色">{{ currentItem.itemCondition }}</el-descriptions-item>
        <el-descriptions-item label="数量">{{ currentItem.quantity }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusTag(currentItem.status)">{{ currentItem.status }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="自提点">{{ currentItem.pickupPointName }}</el-descriptions-item>
        <el-descriptions-item label="发布人">{{ currentItem.publisherName }}</el-descriptions-item>
        <el-descriptions-item label="联系方式">{{ currentItem.contactPhone }}</el-descriptions-item>
        <el-descriptions-item label="浏览量">{{ currentItem.viewCount }}</el-descriptions-item>
        <el-descriptions-item label="互换次数">{{ currentItem.exchangeCount }}</el-descriptions-item>
        <el-descriptions-item label="发布时间">{{ currentItem.createTime }}</el-descriptions-item>
        <el-descriptions-item label="物品描述" :span="2">{{ currentItem.description }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <el-dialog v-model="exchangeVisible" title="申请互换" width="500px">
      <el-form :model="exchangeForm" label-width="100px">
        <el-form-item label="目标物品">
          <el-input :model-value="currentItem?.name" disabled />
        </el-form-item>
        <el-form-item label="提供物品">
          <el-select v-model="exchangeForm.myItemId" placeholder="选择您的物品" style="width: 100%">
            <el-option v-for="item in myItems" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="互换说明">
          <el-input v-model="exchangeForm.remark" type="textarea" :rows="3" placeholder="请输入互换说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="exchangeVisible = false">取消</el-button>
        <el-button type="primary" @click="submitExchange">提交申请</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { api } from '@/api'

const router = useRouter()
const loading = ref(false)
const total = ref(0)
const tableData = ref([])
const selectedItems = ref([])
const detailVisible = ref(false)
const exchangeVisible = ref(false)
const currentItem = ref(null)
const myItems = ref([])

const queryForm = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  category: '',
  status: '',
  condition: ''
})

const categories = [
  { label: '书籍', value: '书籍' },
  { label: '家居', value: '家居' },
  { label: '电子', value: '电子' },
  { label: '衣物', value: '衣物' },
  { label: '玩具', value: '玩具' },
  { label: '其他', value: '其他' }
]

const statusOptions = [
  { label: '可互换', value: '可互换' },
  { label: '互换中', value: '互换中' },
  { label: '已下架', value: '已下架' },
  { label: '已归档', value: '已归档' }
]

const conditionOptions = [
  { label: '全新', value: '全新' },
  { label: '九成新', value: '九成新' },
  { label: '八成新', value: '八成新' },
  { label: '七成新', value: '七成新' },
  { label: '六成新', value: '六成新' }
]

const exchangeForm = reactive({
  myItemId: null,
  remark: ''
})

const getStatusTag = (status) => {
  const map = { '可互换': 'success', '互换中': 'warning', '已下架': 'info', '已归档': 'danger' }
  return map[status] || 'info'
}

const getConditionTag = (condition) => {
  const map = { '全新': 'success', '九成新': 'primary', '八成新': 'info', '七成新': 'warning', '六成新': 'danger' }
  return map[condition] || 'info'
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await api.idleItem.page(queryForm)
    tableData.value = res.list
    total.value = res.total
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  Object.assign(queryForm, { pageNum: 1, pageSize: 10, keyword: '', category: '', status: '', condition: '' })
  loadData()
}

const handleSizeChange = () => {
  queryForm.pageNum = 1
  loadData()
}

const handleSelectionChange = (val) => {
  selectedItems.value = val
}

const handleView = (row) => {
  currentItem.value = row
  detailVisible.value = true
}

const handleExchange = async (row) => {
  currentItem.value = row
  try {
    const res = await api.idleItem.getMyItems()
    myItems.value = res.list
    exchangeForm.myItemId = null
    exchangeForm.remark = ''
    exchangeVisible.value = true
  } catch (e) {
    console.error(e)
  }
}

const submitExchange = async () => {
  if (!exchangeForm.myItemId) {
    ElMessage.warning('请选择要互换的物品')
    return
  }
  try {
    await api.exchangeApply.apply({
      targetItemId: currentItem.value.id,
      myItemId: exchangeForm.myItemId,
      remark: exchangeForm.remark
    })
    ElMessage.success('互换申请已提交')
    exchangeVisible.value = false
    loadData()
  } catch (e) {
    console.error(e)
  }
}

const handleOffline = async (row) => {
  ElMessageBox.confirm('确定要下架该物品吗？', '提示', { type: 'warning' }).then(async () => {
    await api.idleItem.offline(row.id)
    ElMessage.success('下架成功')
    loadData()
  }).catch(() => {})
}

const handleBatchOffline = async () => {
  const ids = selectedItems.value.map(item => item.id)
  ElMessageBox.confirm(`确定要下架选中的 ${ids.length} 件物品吗？`, '提示', { type: 'warning' }).then(async () => {
    await api.idleItem.batchOffline(ids)
    ElMessage.success('批量下架成功')
    loadData()
  }).catch(() => {})
}

const handleArchive = async (row) => {
  ElMessageBox.confirm('确定要归档该物品吗？归档后不可恢复。', '提示', { type: 'warning' }).then(async () => {
    await api.itemArchive.archive(row.id)
    ElMessage.success('归档成功')
    loadData()
  }).catch(() => {})
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.item-name {
  display: flex;
  gap: 12px;
  .item-thumb {
    width: 48px;
    height: 48px;
    border-radius: 6px;
    overflow: hidden;
    flex-shrink: 0;
    background: $bg-primary;
    img { width: 100%; height: 100%; object-fit: cover; }
  }
  .item-text {
    .name { font-weight: 500; color: $text-primary; }
    .desc { font-size: 12px; color: $text-secondary; margin-top: 4px; overflow: hidden; text-overflow: ellipsis; display: -webkit-box; -webkit-line-clamp: 1; -webkit-box-orient: vertical; }
  }
}
</style>
