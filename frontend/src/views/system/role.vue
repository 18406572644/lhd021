<template>
  <div class="role-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span class="title">角色管理</span>
          <el-button type="primary" @click="handleAdd" v-permission="'system_role_add'">
            <el-icon><Plus /></el-icon>
            新增角色
          </el-button>
        </div>
      </template>
      
      <el-table :data="roleList" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="roleCode" label="角色编码" width="160" />
        <el-table-column prop="roleName" label="角色名称" width="160" />
        <el-table-column prop="roleDesc" label="角色描述" min-width="200" />
        <el-table-column label="数据权限" width="120">
          <template #default="{ row }">
            <el-tag :type="getDataScopeTagType(row.dataScope)">
              {{ getDataScopeText(row.dataScope) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button 
              type="primary" 
              link 
              @click="handleAssignPermission(row)"
              v-permission="'system_role_assign_permission'"
            >
              分配权限
            </el-button>
            <el-button 
              type="warning" 
              link 
              @click="handleAssignDataPermission(row)"
              v-permission="'system_role_assign_permission'"
            >
              数据权限
            </el-button>
            <el-button 
              type="primary" 
              link 
              @click="handleEdit(row)"
              v-permission="'system_role_edit'"
            >
              编辑
            </el-button>
            <el-button 
              type="danger" 
              link 
              @click="handleDelete(row)"
              v-permission="'system_role_delete'"
            >
              删除
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
      v-model="dialogVisible" 
      :title="dialogTitle" 
      width="600px"
      @close="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="角色编码" prop="roleCode">
          <el-input v-model="form.roleCode" placeholder="请输入角色编码" />
        </el-form-item>
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色描述" prop="roleDesc">
          <el-input v-model="form.roleDesc" type="textarea" placeholder="请输入角色描述" />
        </el-form-item>
        <el-form-item label="数据权限" prop="dataScope">
          <el-select v-model="form.dataScope" placeholder="请选择数据权限范围">
            <el-option label="全部数据" :value="1" />
            <el-option label="本人数据" :value="4" />
            <el-option label="自定义数据" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
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
    
    <el-dialog 
      v-model="permissionDialogVisible" 
      :title="`分配权限 - ${currentRole?.roleName}`" 
      width="600px"
    >
      <el-tree
        ref="treeRef"
        :data="permissionTree"
        show-checkbox
        node-key="id"
        :default-checked-keys="checkedPermissions"
        :props="{ label: 'permissionName', children: 'children' }"
      />
      <template #footer>
        <el-button @click="permissionDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitPermission">确定</el-button>
      </template>
    </el-dialog>
    
    <el-dialog 
      v-model="dataPermissionDialogVisible" 
      :title="`数据权限配置 - ${currentRole?.roleName}`" 
      width="600px"
    >
      <el-form ref="dataFormRef" :model="dataForm" label-width="100px">
        <el-form-item label="业务类型">
          <el-select v-model="dataForm.businessType" placeholder="请选择业务类型">
            <el-option label="自提点" value="PICKUP_POINT" />
          </el-select>
        </el-form-item>
        <el-form-item label="自提点">
          <el-select 
            v-model="dataForm.businessIds" 
            multiple 
            placeholder="请选择可访问的自提点"
            style="width: 100%"
          >
            <el-option 
              v-for="point in pickupPointList" 
              :key="point.id" 
              :label="point.pointName" 
              :value="point.id" 
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dataPermissionDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitDataPermission">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { systemApi } from '@/api'
import { pickupPointApi } from '@/api'

const loading = ref(false)
const roleList = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const dialogVisible = ref(false)
const dialogTitle = ref('新增角色')
const permissionDialogVisible = ref(false)
const dataPermissionDialogVisible = ref(false)
const formRef = ref()
const treeRef = ref()
const dataFormRef = ref()
const currentRole = ref()
const permissionTree = ref([])
const pickupPointList = ref([])
const checkedPermissions = ref([])

const form = ref({
  id: null,
  roleCode: '',
  roleName: '',
  roleDesc: '',
  dataScope: 1,
  status: 1,
  sortOrder: 0
})

const dataForm = ref({
  businessType: 'PICKUP_POINT',
  businessIds: []
})

const rules = {
  roleCode: [{ required: true, message: '请输入角色编码', trigger: 'blur' }],
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }]
}

const getDataScopeText = (scope) => {
  const map = { 1: '全部', 2: '本部门及以下', 3: '本部门', 4: '本人', 5: '自定义' }
  return map[scope] || '未知'
}

const getDataScopeTagType = (scope) => {
  const map = { 1: '', 2: 'success', 3: 'warning', 4: 'info', 5: 'danger' }
  return map[scope] || 'info'
}

const fetchRoleList = async () => {
  loading.value = true
  try {
    const res = await systemApi.getRolePage({ pageNum: pageNum.value, pageSize: pageSize.value })
    roleList.value = res.list || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

const fetchPermissionTree = async () => {
  const res = await systemApi.getPermissionTree()
  permissionTree.value = res || []
}

const fetchPickupPointList = async () => {
  const res = await pickupPointApi.listAll()
  pickupPointList.value = res || []
}

const handleAdd = () => {
  dialogTitle.value = '新增角色'
  form.value = { id: null, roleCode: '', roleName: '', roleDesc: '', dataScope: 1, status: 1, sortOrder: 0 }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑角色'
  form.value = { ...row }
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该角色吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await systemApi.deleteRole(row.id)
    ElMessage.success('删除成功')
    fetchRoleList()
  }).catch(() => {})
}

const handleAssignPermission = async (row) => {
  currentRole.value = row
  checkedPermissions.value = []
  await fetchPermissionTree()
  
  try {
    const detail = await systemApi.getRoleDetail(row.id)
    checkedPermissions.value = detail.permissionIds || []
    setTimeout(() => {
      treeRef.value?.setCheckedKeys(checkedPermissions.value)
    }, 100)
  } catch (e) {
    console.error('获取角色详情失败', e)
  }
  
  permissionDialogVisible.value = true
}

const handleAssignDataPermission = async (row) => {
  currentRole.value = row
  dataForm.value = { businessType: 'PICKUP_POINT', businessIds: [] }
  await fetchPickupPointList()
  
  try {
    const detail = await systemApi.getRoleDetail(row.id)
    if (detail.dataPermissions) {
      const pp = detail.dataPermissions.find(p => p.businessType === 'PICKUP_POINT')
      if (pp) {
        dataForm.value.businessIds = pp.businessIds || []
      }
    }
  } catch (e) {
    console.error('获取角色详情失败', e)
  }
  
  dataPermissionDialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value?.validate()
  if (form.value.id) {
    await systemApi.updateRole(form.value)
    ElMessage.success('更新成功')
  } else {
    await systemApi.addRole(form.value)
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
  fetchRoleList()
}

const handleSubmitPermission = async () => {
  const checkedKeys = treeRef.value?.getCheckedKeys() || []
  await systemApi.assignPermissions(currentRole.value.id, checkedKeys)
  ElMessage.success('权限分配成功')
  permissionDialogVisible.value = false
}

const handleSubmitDataPermission = async () => {
  await systemApi.assignDataPermissions(currentRole.value.id, dataForm.value)
  ElMessage.success('数据权限分配成功')
  dataPermissionDialogVisible.value = false
}

const handleSizeChange = (val) => {
  pageSize.value = val
  pageNum.value = 1
  fetchRoleList()
}

const handleCurrentChange = (val) => {
  pageNum.value = val
  fetchRoleList()
}

const resetForm = () => {
  formRef.value?.resetFields()
}

onMounted(() => {
  fetchRoleList()
})
</script>

<style scoped lang="scss">
.role-management {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    .title {
      font-size: 16px;
      font-weight: 600;
    }
  }
  .pagination {
    margin-top: 20px;
    justify-content: flex-end;
    display: flex;
  }
}
</style>
