<template>
  <div class="page-container">
    <div class="page-header">
      <h2>互换申请管理</h2>
      <el-steps :active="2" finish-status="success" class="mini-steps" v-if="userStore.userInfo?.role === 'ADMIN'">
        <el-step title="待审核" />
        <el-step title="审核通过" />
        <el-step title="已完成" />
      </el-steps>
    </div>

    <div class="page-card">
      <div class="search-form">
        <el-form :inline="true" :model="queryForm">
          <el-form-item label="状态">
            <el-select v-model="queryForm.status" placeholder="全部状态" clearable style="width: 140px">
              <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="申请人">
            <el-input v-model="queryForm.applicantName" placeholder="申请人" clearable style="width: 140px" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadData">查询</el-button>
            <el-button @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table :data="tableData" v-loading="loading">
        <el-table-column prop="id" label="申请单号" width="100" />
        <el-table-column label="互换信息" min-width="260">
          <template #default="{ row }">
            <div class="exchange-info">
              <div class="exchange-item">
                <span class="label">我的物品:</span>
                <span class="value">{{ row.myItemName }}</span>
              </div>
              <div class="exchange-arrow">
                <el-icon><Right /></el-icon>
              </div>
              <div class="exchange-item">
                <span class="label">对方物品:</span>
                <span class="value">{{ row.targetItemName }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="applicantName" label="申请人" width="100" />
        <el-table-column prop="targetUserName" label="对方用户" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="getStatusTag(row.status)">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="互换说明" min-width="150" show-overflow-tooltip />
        <el-table-column prop="applyTime" label="申请时间" width="160" />
        <el-table-column prop="auditTime" label="审核时间" width="160" />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">查看</el-button>
            <el-button link type="success" @click="handleApprove(row)" v-if="row.status === '待审核' && isAdmin">通过</el-button>
            <el-button link type="danger" @click="handleReject(row)" v-if="row.status === '待审核' && isAdmin">拒绝</el-button>
            <el-button link type="warning" @click="handleComplete(row)" v-if="row.status === '审核通过' && !row.claimRecordId">生成领用单</el-button>
            <el-button link type="info" @click="handleCancel(row)" v-if="row.status === '待审核'">取消</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="queryForm.pageNum"
          v-model:page-size="queryForm.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="loadData"
          @size-change="handleSizeChange"
        />
      </div>
    </div>

    <el-dialog v-model="detailVisible" title="申请详情" width="650px">
      <el-descriptions :column="2" border v-if="currentItem">
        <el-descriptions-item label="申请单号">{{ currentItem.id }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusTag(currentItem.status)">{{ currentItem.status }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="申请人" :span="2">{{ currentItem.applicantName }}</el-descriptions-item>
        <el-descriptions-item label="我的物品">{{ currentItem.myItemName }}</el-descriptions-item>
        <el-descriptions-item label="对方物品">{{ currentItem.targetItemName }}</el-descriptions-item>
        <el-descriptions-item label="对方用户">{{ currentItem.targetUserName }}</el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ currentItem.applyTime }}</el-descriptions-item>
        <el-descriptions-item label="审核时间">{{ currentItem.auditTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="审核人">{{ currentItem.auditorName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="互换说明" :span="2">{{ currentItem.remark || '-' }}</el-descriptions-item>
        <el-descriptions-item label="审核意见" :span="2">{{ currentItem.auditRemark || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <el-dialog v-model="rejectVisible" title="拒绝申请" width="500px">
      <el-form :model="rejectForm" label-width="80px">
        <el-form-item label="拒绝原因">
          <el-input v-model="rejectForm.auditRemark" type="textarea" :rows="4" placeholder="请输入拒绝原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="danger" @click="submitReject">确认拒绝</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { api } from '@/api'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const loading = ref(false)
const total = ref(0)
const tableData = ref([])
const detailVisible = ref(false)
const rejectVisible = ref(false)
const currentItem = ref(null)

const isAdmin = computed(() => userStore.userInfo?.role === 'ADMIN')

const queryForm = reactive({
  pageNum: 1,
  pageSize: 10,
  status: '',
  applicantName: ''
})

const statusOptions = [
  { label: '待审核', value: '待审核' },
  { label: '审核通过', value: '审核通过' },
  { label: '已拒绝', value: '已拒绝' },
  { label: '已完成', value: '已完成' },
  { label: '已取消', value: '已取消' }
]

const rejectForm = reactive({
  auditRemark: ''
})

const getStatusTag = (status) => {
  const map = { '待审核': 'warning', '审核通过': 'success', '已拒绝': 'danger', '已完成': 'info', '已取消': 'info' }
  return map[status] || 'info'
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await api.exchangeApply.page(queryForm)
    tableData.value = res.data.records
    total.value = res.data.total
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  Object.assign(queryForm, { pageNum: 1, pageSize: 10, status: '', applicantName: '' })
  loadData()
}

const handleSizeChange = () => {
  queryForm.pageNum = 1
  loadData()
}

const handleView = (row) => {
  currentItem.value = row
  detailVisible.value = true
}

const handleApprove = async (row) => {
  ElMessageBox.confirm('确定通过该互换申请吗？', '提示', { type: 'success' }).then(async () => {
    await api.exchangeApply.approve(row.id)
    ElMessage.success('审核通过')
    loadData()
  }).catch(() => {})
}

const handleReject = (row) => {
  currentItem.value = row
  rejectForm.auditRemark = ''
  rejectVisible.value = true
}

const submitReject = async () => {
  try {
    await api.exchangeApply.reject(currentItem.value.id, { auditRemark: rejectForm.auditRemark })
    ElMessage.success('已拒绝')
    rejectVisible.value = false
    loadData()
  } catch (e) {
    console.error(e)
  }
}

const handleComplete = async (row) => {
  ElMessageBox.confirm('确定生成领用单吗？', '提示', { type: 'warning' }).then(async () => {
    await api.exchangeApply.complete(row.id)
    ElMessage.success('已生成领用单，请前往领用记录查看')
    loadData()
  }).catch(() => {})
}

const handleCancel = async (row) => {
  ElMessageBox.confirm('确定取消该申请吗？', '提示', { type: 'warning' }).then(async () => {
    await api.exchangeApply.cancel(row.id)
    ElMessage.success('已取消')
    loadData()
  }).catch(() => {})
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.mini-steps {
  width: 400px;
  :deep(.el-step__title) { font-size: 13px; }
}
.exchange-info {
  display: flex;
  align-items: center;
  gap: 12px;
  .exchange-item {
    flex: 1;
    padding: 8px 12px;
    background: $bg-primary;
    border-radius: 6px;
    .label { color: $text-secondary; font-size: 12px; display: block; margin-bottom: 4px; }
    .value { color: $text-primary; font-weight: 500; }
  }
  .exchange-arrow {
    color: $primary-color;
    font-size: 20px;
  }
}
</style>
