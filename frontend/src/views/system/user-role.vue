<template>
  <div class="user-role-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span class="title">用户角色管理</span>
        </div>
      </template>
      
      <el-table :data="userList" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="140" />
        <el-table-column prop="nickname" label="昵称" width="140" />
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column label="信用分" width="100">
          <template #default="{ row }">
            {{ row.creditScore }}
          </template>
        </el-table-column>
        <el-table-column label="信用等级" width="100">
          <template #default="{ row }">
            <el-tag :type="getCreditLevelTag(row.creditLevel)">
              {{ row.creditLevel }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="角色" min-width="200">
          <template #default="{ row }">
            <el-tag 
              v-for="role in row.roles" 
              :key="role.id" 
              size="small"
              style="margin-right: 5px; margin-bottom: 5px;"
            >
              {{ role.roleName }}
            </el-tag>
            <span v-if="!row.roles || row.roles.length === 0" class="text-muted">未分配角色</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button 
              type="primary" 
              link 
              @click="handleAssignRole(row)"
              v-permission="'system_user_assign_role'"
            >
              分配角色
            </el-button>
            <el-button 
              v-if="row.status === 1"
              type="danger" 
              link 
              @click="handleDisable(row)"
              v-permission="'system_user_edit'"
            >
              禁用
            </el-button>
            <el-button 
              v-else
              type="success" 
              link 
              @click="handleEnable(row)"
              v-permission="'system_user_edit'"
            >
              启用
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
      :title="`分配角色 - ${currentUser?.nickname || currentUser?.username}`" 
      width="500px"
    >
      <el-checkbox-group v-model="checkedRoleIds">
        <el-checkbox 
          v-for="role in roleList" 
          :key="role.id" 
          :label="role.id"
          style="display: block; margin-bottom: 10px;"
        >
          <span>{{ role.roleName }}</span>
          <el-tag size="small" style="margin-left: 8px;">{{ role.roleCode }}</el-tag>
        </el-checkbox>
      </el-checkbox-group>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { authApi, systemApi } from '@/api'

const loading = ref(false)
const userList = ref([])
const roleList = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const dialogVisible = ref(false)
const currentUser = ref()
const checkedRoleIds = ref([])

const fetchUserList = async () => {
  loading.value = true
  try {
    const res = await authApi.listUsers()
    userList.value = res || []
    total.value = userList.value.length
    
    for (let user of userList.value) {
      try {
        const roles = await systemApi.getUserRoles(user.id)
        user.roles = roles || []
      } catch (e) {
        user.roles = []
      }
    }
  } finally {
    loading.value = false
  }
}

const fetchRoleList = async () => {
  const res = await systemApi.getRoleList()
  roleList.value = res || []
}

const handleAssignRole = async (row) => {
  currentUser.value = row
  checkedRoleIds.value = row.roles?.map(r => r.id) || []
  await fetchRoleList()
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await systemApi.assignUserRoles(currentUser.value.id, checkedRoleIds.value)
  ElMessage.success('角色分配成功')
  dialogVisible.value = false
  fetchUserList()
}

const getCreditLevelTag = (level) => {
  const levelMap = {
    '优秀': 'success',
    '良好': 'primary',
    '一般': 'warning',
    '较差': 'info',
    '很差': 'danger'
  }
  return levelMap[level] || 'info'
}

const handleDisable = async (row) => {
  try {
    await systemApi.disableUser(row.id)
    ElMessage.success('用户禁用成功')
    fetchUserList()
  } catch (error) {
    console.error('禁用用户失败:', error)
  }
}

const handleEnable = async (row) => {
  try {
    await systemApi.enableUser(row.id)
    ElMessage.success('用户启用成功')
    fetchUserList()
  } catch (error) {
    console.error('启用用户失败:', error)
  }
}

const handleSizeChange = (val) => {
  pageSize.value = val
  pageNum.value = 1
}

const handleCurrentChange = (val) => {
  pageNum.value = val
}

onMounted(() => {
  fetchUserList()
})
</script>

<style scoped lang="scss">
.user-role-management {
  .card-header {
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
  .text-muted {
    color: #909399;
    font-size: 14px;
  }
}
</style>
