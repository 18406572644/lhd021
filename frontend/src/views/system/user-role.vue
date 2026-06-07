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
        <el-table-column label="角色" min-width="300">
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
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button 
              type="primary" 
              link 
              @click="handleAssignRole(row)"
              v-permission="'system:user:assign'"
            >
              分配角色
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
