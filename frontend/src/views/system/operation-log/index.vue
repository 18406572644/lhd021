<template>
  <div class="operation-log">
    <el-card>
      <template #header>
        <div class="card-header">
          <span class="title">操作日志</span>
          <el-button type="primary" @click="handleExport" v-permission="'system:operation-log:export'">
            <el-icon><Download /></el-icon>
            导出日志
          </el-button>
        </div>
      </template>

      <el-form :inline="true" :model="queryForm" class="query-form">
        <el-form-item label="操作人">
          <el-input
            v-model="queryForm.operatorName"
            placeholder="请输入操作人用户名"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="操作类型">
          <el-select
            v-model="queryForm.operationType"
            placeholder="请选择操作类型"
            clearable
            style="width: 180px"
          >
            <el-option
              v-for="item in operationTypeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="操作时间">
          <el-date-picker
            v-model="queryForm.dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>

      <el-table :data="logList" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="operationTypeName" label="操作类型" width="130">
          <template #default="{ row }">
            <el-tag :type="getOperationTypeTag(row.operationType)">
              {{ row.operationTypeName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operationName" label="操作名称" min-width="150" />
        <el-table-column prop="operatorName" label="操作人" width="120" />
        <el-table-column prop="operationTime" label="操作时间" width="180" />
        <el-table-column prop="operationIp" label="操作IP" width="130" />
        <el-table-column prop="targetName" label="操作对象" min-width="180" show-overflow-tooltip />
        <el-table-column prop="changeSummary" label="变更摘要" min-width="250" show-overflow-tooltip />
        <el-table-column label="敏感操作" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.isSensitive === 1" type="danger">是</el-tag>
            <el-tag v-else type="info">否</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="已确认" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.confirmed === 1" type="success">是</el-tag>
            <el-tag v-else type="warning">否</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              link
              @click="handleViewDetail(row)"
              v-permission="'system:operation-log:view'"
            >
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        class="pagination"
        background
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        :page-size="pageSize"
        :current-page="pageNum"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </el-card>

    <el-dialog
      v-model="detailDialogVisible"
      title="操作日志详情"
      width="800px"
    >
      <el-descriptions v-if="currentLog" :column="2" border>
        <el-descriptions-item label="操作类型">
          <el-tag :type="getOperationTypeTag(currentLog.operationType)">
            {{ currentLog.operationTypeName }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="操作名称">
          {{ currentLog.operationName }}
        </el-descriptions-item>
        <el-descriptions-item label="操作人">
          {{ currentLog.operatorName }}
        </el-descriptions-item>
        <el-descriptions-item label="操作时间">
          {{ currentLog.operationTime }}
        </el-descriptions-item>
        <el-descriptions-item label="操作IP">
          {{ currentLog.operationIp }}
        </el-descriptions-item>
        <el-descriptions-item label="操作对象">
          {{ currentLog.targetName || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="敏感操作" :span="2">
          <el-tag v-if="currentLog.isSensitive === 1" type="danger">是</el-tag>
          <el-tag v-else type="info">否</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="已二次确认" :span="2">
          <el-tag v-if="currentLog.confirmed === 1" type="success">是</el-tag>
          <el-tag v-else type="warning">否</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="变更摘要" :span="2">
          {{ currentLog.changeSummary || '无' }}
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">
          {{ currentLog.remark || '无' }}
        </el-descriptions-item>
      </el-descriptions>

      <el-tabs v-if="currentLog && (currentLog.beforeData || currentLog.afterData)" class="detail-tabs">
        <el-tab-pane label="变更前数据" name="before">
          <pre class="json-display">{{ formatJson(currentLog.beforeData) }}</pre>
        </el-tab-pane>
        <el-tab-pane label="变更后数据" name="after">
          <pre class="json-display">{{ formatJson(currentLog.afterData) }}</pre>
        </el-tab-pane>
      </el-tabs>

      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Download } from '@element-plus/icons-vue'
import { operationLogApi } from '@/api'

const loading = ref(false)
const logList = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const detailDialogVisible = ref(false)
const currentLog = ref(null)

const queryForm = reactive({
  operatorName: '',
  operationType: '',
  dateRange: []
})

const operationTypeOptions = [
  { value: 'EXCHANGE_APPROVE', label: '审核通过' },
  { value: 'EXCHANGE_REJECT', label: '审核拒绝' },
  { value: 'ITEM_OFFLINE', label: '物品下架' },
  { value: 'ITEM_DELETE', label: '物品删除' },
  { value: 'CREDIT_ADJUST', label: '信用分调整' },
  { value: 'USER_DISABLE', label: '用户禁用' },
  { value: 'USER_ENABLE', label: '用户启用' },
  { value: 'PICKUP_POINT_ADD', label: '新增自提点' },
  { value: 'PICKUP_POINT_EDIT', label: '编辑自提点' },
  { value: 'PICKUP_POINT_DELETE', label: '删除自提点' },
  { value: 'ROLE_ASSIGN', label: '角色分配' },
  { value: 'ROLE_ADD', label: '新增角色' },
  { value: 'ROLE_EDIT', label: '编辑角色' },
  { value: 'ROLE_DELETE', label: '删除角色' },
  { value: 'PERMISSION_CONFIG', label: '权限配置' },
  { value: 'OTHER', label: '其他操作' }
]

const sensitiveTypes = ['CREDIT_ADJUST', 'USER_DISABLE', 'USER_ENABLE', 'PICKUP_POINT_DELETE', 'ROLE_ASSIGN', 'ROLE_ADD', 'ROLE_EDIT', 'ROLE_DELETE', 'PERMISSION_CONFIG']

const getOperationTypeTag = (type) => {
  if (sensitiveTypes.includes(type)) {
    return 'danger'
  }
  return 'primary'
}

const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      operatorName: queryForm.operatorName || undefined,
      operationType: queryForm.operationType || undefined
    }
    if (queryForm.dateRange && queryForm.dateRange.length === 2) {
      params.startTime = queryForm.dateRange[0]
      params.endTime = queryForm.dateRange[1]
    }
    const data = await operationLogApi.page(params)
    logList.value = data.list
    total.value = data.total
  } catch (error) {
    console.error('获取操作日志失败:', error)
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  pageNum.value = 1
  fetchData()
}

const handleReset = () => {
  queryForm.operatorName = ''
  queryForm.operationType = ''
  queryForm.dateRange = []
  pageNum.value = 1
  fetchData()
}

const handleSizeChange = (size) => {
  pageSize.value = size
  fetchData()
}

const handleCurrentChange = (page) => {
  pageNum.value = page
  fetchData()
}

const handleViewDetail = async (row) => {
  try {
    const data = await operationLogApi.detail(row.id)
    currentLog.value = data
    detailDialogVisible.value = true
  } catch (error) {
    console.error('获取日志详情失败:', error)
  }
}

const handleExport = () => {
  const params = {
    operatorName: queryForm.operatorName || undefined,
    operationType: queryForm.operationType || undefined
  }
  if (queryForm.dateRange && queryForm.dateRange.length === 2) {
    params.startTime = queryForm.dateRange[0]
    params.endTime = queryForm.dateRange[1]
  }
  operationLogApi.export(params)
  ElMessage.success('正在导出日志，请稍候...')
}

const formatJson = (jsonStr) => {
  if (!jsonStr) {
    return '无数据'
  }
  try {
    return JSON.stringify(JSON.parse(jsonStr), null, 2)
  } catch (e) {
    return jsonStr
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped lang="scss">
.operation-log {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .title {
      font-size: 16px;
      font-weight: 600;
    }
  }

  .query-form {
    margin-bottom: 20px;
  }

  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }

  .detail-tabs {
    margin-top: 20px;
  }

  .json-display {
    background: #f5f5f5;
    padding: 15px;
    border-radius: 4px;
    max-height: 400px;
    overflow-y: auto;
    font-size: 13px;
    line-height: 1.6;
    color: #333;
    white-space: pre-wrap;
    word-break: break-all;
  }
}
</style>
