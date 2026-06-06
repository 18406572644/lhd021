<template>
  <div class="page-container">
    <div class="page-header">
      <h2>自提点管理</h2>
      <div class="header-actions">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>新增自提点
        </el-button>
      </div>
    </div>

    <div class="page-card">
      <div class="search-form">
        <el-form :inline="true" :model="queryForm">
          <el-form-item label="名称">
            <el-input v-model="queryForm.name" placeholder="自提点名称" clearable style="width: 180px" />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="queryForm.status" placeholder="全部状态" clearable style="width: 140px">
              <el-option label="启用" value="启用" />
              <el-option label="停用" value="停用" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadData">查询</el-button>
            <el-button @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table :data="tableData" v-loading="loading">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="name" label="自提点名称" min-width="160" />
        <el-table-column prop="address" label="地址" min-width="200" show-overflow-tooltip />
        <el-table-column prop="manager" label="负责人" width="100" />
        <el-table-column prop="phone" label="联系电话" width="130" />
        <el-table-column label="库存情况" width="150">
          <template #default="{ row }">
            <div class="stock-info">
              <el-progress :percentage="(row.currentStock / row.capacity) * 100" :stroke-width="10" :color="getStockColor(row)">
                <span style="font-size: 12px">{{ row.currentStock }}/{{ row.capacity }}</span>
              </el-progress>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag size="small" :type="row.status === '启用' ? 'success' : 'info'">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">查看</el-button>
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="warning" @click="handleToggle(row)" v-if="row.status === '启用'">停用</el-button>
            <el-button link type="success" @click="handleToggle(row)" v-else>启用</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
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

    <el-dialog v-model="formVisible" :title="isEdit ? '编辑自提点' : '新增自提点'" width="600px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="自提点名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入名称" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="form.address" placeholder="请输入详细地址" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="负责人" prop="manager">
              <el-input v-model="form.manager" placeholder="请输入负责人姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="容量" prop="capacity">
              <el-input-number v-model="form.capacity" :min="1" :max="10000" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="当前库存" prop="currentStock">
              <el-input-number v-model="form.currentStock" :min="0" :max="form.capacity" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注（选填）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="loading" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="自提点详情" width="600px">
      <el-descriptions :column="2" border v-if="currentItem">
        <el-descriptions-item label="自提点名称">{{ currentItem.name }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentItem.status === '启用' ? 'success' : 'info'">{{ currentItem.status }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="地址" :span="2">{{ currentItem.address }}</el-descriptions-item>
        <el-descriptions-item label="负责人">{{ currentItem.manager }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ currentItem.phone }}</el-descriptions-item>
        <el-descriptions-item label="容量">{{ currentItem.capacity }}</el-descriptions-item>
        <el-descriptions-item label="当前库存">{{ currentItem.currentStock }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentItem.createTime }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentItem.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { api } from '@/api'

const loading = ref(false)
const total = ref(0)
const tableData = ref([])
const formVisible = ref(false)
const detailVisible = ref(false)
const isEdit = ref(false)
const currentItem = ref(null)
const formRef = ref()

const queryForm = reactive({
  pageNum: 1,
  pageSize: 10,
  name: '',
  status: ''
})

const form = reactive({
  id: null,
  name: '',
  address: '',
  manager: '',
  phone: '',
  capacity: 100,
  currentStock: 0,
  remark: ''
})

const rules = {
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  address: [{ required: true, message: '请输入地址', trigger: 'blur' }],
  manager: [{ required: true, message: '请输入负责人', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  capacity: [{ required: true, message: '请输入容量', trigger: 'blur' }]
}

const getStockColor = (row) => {
  const ratio = row.currentStock / row.capacity
  if (ratio >= 0.9) return '#f56c6c'
  if (ratio >= 0.7) return '#e6a23c'
  return '#67c23a'
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await api.pickupPoint.page(queryForm)
    tableData.value = res.data.records
    total.value = res.data.total
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  Object.assign(queryForm, { pageNum: 1, pageSize: 10, name: '', status: '' })
  loadData()
}

const handleSizeChange = () => {
  queryForm.pageNum = 1
  loadData()
}

const handleAdd = () => {
  isEdit.value = false
  Object.assign(form, { id: null, name: '', address: '', manager: '', phone: '', capacity: 100, currentStock: 0, remark: '' })
  formVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(form, { ...row })
  formVisible.value = true
}

const handleView = (row) => {
  currentItem.value = row
  detailVisible.value = true
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    loading.value = true
    if (isEdit.value) {
      await api.pickupPoint.update(form)
      ElMessage.success('更新成功')
    } else {
      await api.pickupPoint.add(form)
      ElMessage.success('新增成功')
    }
    formVisible.value = false
    loadData()
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleToggle = async (row) => {
  const action = row.status === '启用' ? '停用' : '启用'
  ElMessageBox.confirm(`确定${action}该自提点吗？`, '提示', { type: 'warning' }).then(async () => {
    if (row.status === '启用') {
      await api.pickupPoint.disable(row.id)
    } else {
      await api.pickupPoint.enable(row.id)
    }
    ElMessage.success(`${action}成功`)
    loadData()
  }).catch(() => {})
}

const handleDelete = async (row) => {
  ElMessageBox.confirm('确定删除该自提点吗？有库存时无法删除。', '提示', { type: 'warning' }).then(async () => {
    await api.pickupPoint.delete(row.id)
    ElMessage.success('删除成功')
    loadData()
  }).catch(() => {})
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.stock-info {
  padding: 0 10px;
}
</style>
