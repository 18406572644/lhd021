<template>
  <div class="permission-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span class="title">权限管理</span>
          <div class="header-actions">
            <el-button type="primary" @click="handleAdd(null)" v-permission="'system:permission:add'">
              <el-icon><Plus /></el-icon>
              新增顶级权限
            </el-button>
          </div>
        </div>
      </template>
      
      <el-table :data="permissionTree" v-loading="loading" row-key="id" stripe default-expand-all>
        <el-table-column prop="permissionName" label="权限名称" min-width="200" />
        <el-table-column prop="permissionCode" label="权限编码" width="180" />
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.permissionType)">
              {{ getTypeText(row.permissionType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.visible === 1 ? 'success' : 'info'">
              {{ row.visible === 1 ? '显示' : '隐藏' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button 
              type="primary" 
              link 
              @click="handleAdd(row)"
              v-permission="'system:permission:add'"
            >
              添加子权限
            </el-button>
            <el-button 
              type="primary" 
              link 
              @click="handleEdit(row)"
              v-permission="'system:permission:edit'"
            >
              编辑
            </el-button>
            <el-button 
              type="danger" 
              link 
              @click="handleDelete(row)"
              v-permission="'system:permission:delete'"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <el-dialog 
      v-model="dialogVisible" 
      :title="dialogTitle" 
      width="600px"
      @close="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="上级权限">
          <el-tree-select
            v-model="form.parentId"
            :data="permissionTree"
            :props="{ label: 'permissionName', value: 'id', children: 'children' }"
            placeholder="顶级权限"
            clearable
            check-strictly
          />
        </el-form-item>
        <el-form-item label="权限类型" prop="permissionType">
          <el-radio-group v-model="form.permissionType">
            <el-radio :value="1">菜单</el-radio>
            <el-radio :value="2">按钮</el-radio>
            <el-radio :value="3">接口</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="权限名称" prop="permissionName">
          <el-input v-model="form.permissionName" placeholder="请输入权限名称" />
        </el-form-item>
        <el-form-item label="权限编码" prop="permissionCode">
          <el-input v-model="form.permissionCode" placeholder="请输入权限编码，如：user:add" />
        </el-form-item>
        <el-form-item v-if="form.permissionType === 1" label="路由路径" prop="path">
          <el-input v-model="form.path" placeholder="请输入路由路径" />
        </el-form-item>
        <el-form-item v-if="form.permissionType === 1" label="组件路径" prop="component">
          <el-input v-model="form.component" placeholder="请输入组件路径，如：system/user" />
        </el-form-item>
        <el-form-item v-if="form.permissionType === 1" label="图标" prop="icon">
          <el-input v-model="form.icon" placeholder="请输入图标名称，如：User" />
        </el-form-item>
        <el-form-item label="是否显示" prop="visible">
          <el-radio-group v-model="form.visible">
            <el-radio :value="1">显示</el-radio>
            <el-radio :value="0">隐藏</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { systemApi } from '@/api'

const loading = ref(false)
const permissionTree = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增权限')
const formRef = ref()

const form = ref({
  id: null,
  parentId: null,
  permissionType: 1,
  permissionName: '',
  permissionCode: '',
  path: '',
  component: '',
  icon: '',
  visible: 1,
  sortOrder: 0
})

const rules = {
  permissionName: [{ required: true, message: '请输入权限名称', trigger: 'blur' }],
  permissionCode: [{ required: true, message: '请输入权限编码', trigger: 'blur' }],
  permissionType: [{ required: true, message: '请选择权限类型', trigger: 'change' }]
}

const getTypeText = (type) => {
  const map = { 1: '菜单', 2: '按钮', 3: '接口' }
  return map[type] || '未知'
}

const getTypeTagType = (type) => {
  const map = { 1: 'success', 2: 'warning', 3: 'info' }
  return map[type] || 'info'
}

const fetchPermissionTree = async () => {
  loading.value = true
  try {
    const res = await systemApi.getPermissionTree()
    permissionTree.value = res || []
  } finally {
    loading.value = false
  }
}

const handleAdd = (row) => {
  dialogTitle.value = row ? '新增子权限' : '新增顶级权限'
  form.value = {
    id: null,
    parentId: row?.id || null,
    permissionType: row ? 2 : 1,
    permissionName: '',
    permissionCode: '',
    path: '',
    component: '',
    icon: '',
    visible: 1,
    sortOrder: 0
  }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑权限'
  form.value = { ...row }
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该权限吗？删除后子权限也会被删除', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await systemApi.deletePermission(row.id)
    ElMessage.success('删除成功')
    fetchPermissionTree()
  }).catch(() => {})
}

const handleSubmit = async () => {
  await formRef.value?.validate()
  if (form.value.id) {
    await systemApi.updatePermission(form.value)
    ElMessage.success('更新成功')
  } else {
    await systemApi.addPermission(form.value)
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
  fetchPermissionTree()
}

const resetForm = () => {
  formRef.value?.resetFields()
}

onMounted(() => {
  fetchPermissionTree()
})
</script>

<style scoped lang="scss">
.permission-management {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    .title {
      font-size: 16px;
      font-weight: 600;
    }
    .header-actions {
      display: flex;
      gap: 10px;
    }
  }
}
</style>
