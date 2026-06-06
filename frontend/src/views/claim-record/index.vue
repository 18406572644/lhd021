<template>
  <div class="page-container">
    <div class="page-header">
      <h2>物品领用记录</h2>
      <el-alert
        title="取件说明"
        type="info"
        :closable="false"
        class="alert-info"
      >
        <template #default>
          <span>用户凭取件码到自提点领取物品，管理员确认领取后更新状态。取件码：

领取成功后双方信用分各+5分。</span>
        </template>
      </el-alert>
    </div>

    <div class="page-card">
      <div class="search-form">
        <el-form :inline="true" :model="queryForm">
          <el-form-item label="状态">
            <el-select v-model="queryForm.status" placeholder="全部状态" clearable style="width: 140px">
              <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="取件码">
            <el-input v-model="queryForm.pickupCode" placeholder="输入取件码" clearable style="width: 140px" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadData">查询</el-button>
            <el-button @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table :data="tableData" v-loading="loading">
        <el-table-column prop="id" label="领用单号" width="110" />
        <el-table-column prop="itemName" label="物品名称" min-width="150" />
        <el-table-column prop="itemName" label="领取人" width="100" />
        <el-table-column prop="pickupPointName" label="自提点" min-width="150" />
        <el-table-column label="取件码" width="120">
          <template #default="{ row }">
            <div class="pickup-code">
              <el-tag size="large" type="warning" effect="dark">{{ row.pickupCode }}</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="getStatusTag(row.status)">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="生成时间" width="160" />
        <el-table-column prop="claimTime" label="领取时间" width="160" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">查看</el-button>
            <el-button link type="success" @click="handleConfirm(row)" v-if="row.status === '待领取' && isAdmin">确认领取</el-button>
            <el-button link type="danger" @click="handleCancel(row)" v-if="row.status === '待领取'">取消领取</el-button>
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

    <el-dialog v-model="detailVisible" title="领用详情" width="600px">
      <el-descriptions :column="2" border v-if="currentItem">
        <el-descriptions-item label="领用单号">{{ currentItem.id }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusTag(currentItem.status)">{{ currentItem.status }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="物品名称">{{ currentItem.itemName }}</el-descriptions-item>
        <el-descriptions-item label="数量">{{ currentItem.quantity }}</el-descriptions-item>
        <el-descriptions-item label="领取人">{{ currentItem.claimantName }}</el-descriptions-item>
        <el-descriptions-item label="取件码">
          <el-tag type="warning" effect="dark">{{ currentItem.pickupCode }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="自提点">{{ currentItem.pickupPointName }}</el-descriptions-item>
        <el-descriptions-item label="联系人">{{ currentItem.contactPhone }}</el-descriptions-item>
        <el-descriptions-item label="生成时间">{{ currentItem.createTime }}</el-descriptions-item>
        <el-descriptions-item label="领取时间">{{ currentItem.claimTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="互换申请号" v-if="currentItem.exchangeApplyId">{{ currentItem.exchangeApplyId }}</el-descriptions-item>
        <el-descriptions-item label="备注">{{ currentItem.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <el-dialog v-model="confirmVisible" title="确认领取" width="500px">
      <el-alert title="请核对取件人提供取件码进行核验" type="warning" :closable="false" style="margin-bottom: 20px;" />
      <el-form :model="confirmForm" label-width="80px">
        <el-form-item label="取件码" prop="pickupCode">
          <el-input v-model="confirmForm.pickupCode" placeholder="请输入6位取件码" maxlength="6" size="large" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="confirmVisible = false">取消</el-button>
        <el-button type="primary" @click="submitConfirm">确认领取</el-button>
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
const confirmVisible = ref(false)
const currentItem = ref(null)

const isAdmin = computed(() => userStore.userInfo?.role === 'ADMIN')

const queryForm = reactive({
  pageNum: 1,
  pageSize: 10,
  status: '',
  pickupCode: ''
})

const statusOptions = [
  { label: '待领取', value: '待领取' },
  { label: '已领取', value: '已领取' },
  { label: '已取消', value: '已取消' },
  { label: '已过期', value: '已过期' }
]

const confirmForm = reactive({
  pickupCode: ''
})

const getStatusTag = (status) => {
  const map = { '待领取': 'warning', '已领取': 'success', '已取消': 'info', '已过期': 'danger' }
  return map[status] || 'info'
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await api.claimRecord.page(queryForm)
    tableData.value = res.data.records
    total.value = res.data.total
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  Object.assign(queryForm, { pageNum: 1, pageSize: 10, status: '', pickupCode: '' })
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

const handleConfirm = (row) => {
  currentItem.value = row
  confirmForm.pickupCode = ''
  confirmVisible.value = true
}

const submitConfirm = async () => {
  if (!confirmForm.pickupCode.length !== 6) {
    ElMessage.warning('请输入6位取件码')
    return
  }
  if (confirmForm.pickupCode !== currentItem.value.pickupCode) {
    ElMessage.error('取件码不正确')
    return
  }
  try {
    await api.claimRecord.confirm(currentItem.value.id, { pickupCode: confirmForm.pickupCode })
    ElMessage.success('领取确认成功，双方信用分各+5分')
    confirmVisible.value = false
    loadData()
  } catch (e) {
    console.error(e)
  }
}

const handleCancel = async (row) => {
  ElMessageBox.confirm('确定取消该领用单吗？', '提示', { type: 'warning' }).then(async () => {
    await api.claimRecord.cancel(row.id)
    ElMessage.success('已取消')
    loadData()
  }).catch(() => {})
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.alert-info {
  margin-bottom: 20px;
}
.pickup-code {
  .el-tag {
    font-size: 18px;
    letter-spacing: 4px;
    padding: 4px 16px;
    font-family: monospace;
  }
}
</style>
