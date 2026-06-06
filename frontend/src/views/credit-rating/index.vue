<template>
  <div class="page-container">
    <div class="credit-header">
      <div class="credit-card" v-if="currentUser">
        <div class="credit-info">
          <div class="credit-avatar">
            <el-avatar :size="64" :icon="UserFilled" />
            <span class="credit-level-badge" :class="getCreditClass(currentUser.creditLevel)">
              {{ currentUser.creditLevel }}
            </span>
          </div>
          <div class="credit-detail">
            <h3>{{ currentUser.nickname || currentUser.username }}</h3>
            <div class="credit-score">
              <span class="score-number">{{ currentUser.creditScore }}</span>
              <span class="score-label">信用分</span>
            </div>
            <div class="credit-stats">
              <div class="stat">
                <span class="num">{{ currentUser.publishCount || 0 }}</span>
                <span class="label">发布数</span>
              </div>
              <div class="stat">
                <span class="num">{{ currentUser.exchangeCount || 0 }}</span>
                <span class="label">互换数</span>
              </div>
              <div class="stat">
                <span class="num">{{ currentUser.claimCount || 0 }}</span>
                <span class="label">领取数</span>
              </div>
            </div>
          </div>
        </div>
        <div class="credit-progress">
          <div class="progress-label">
            <span>信用进度</span>
            <span>{{ currentUser.creditScore }} / 100</span>
          </div>
          <el-progress :percentage="currentUser.creditScore" :stroke-width="12" :color="getProgressColor(currentUser.creditScore)" />
          <div class="level-markers">
            <span>很差</span>
            <span>较差</span>
            <span>一般</span>
            <span>良好</span>
            <span>优秀</span>
          </div>
        </div>
      </div>
      <div class="credit-rules" v-if="userStore.userInfo?.role === 'ADMIN'">
        <div class="rules-title">
          <el-icon><InfoFilled /></el-icon>
          <span>信用评级规则</span>
        </div>
        <ul class="rules-list">
          <li><span class="plus">+10</span> 首次发布物品</li>
          <li><span class="plus">+5</span> 完成一次互换</li>
          <li><span class="plus">+5</span> 完成一次领取</li>
          <li><span class="minus">-10</span> 互换申请违约</li>
          <li><span class="minus">-15</span> 物品描述不符</li>
          <li><span class="minus">-20</span> 恶意举报</li>
        </ul>
        <div class="admin-actions">
          <el-button type="primary" @click="showAdjust = true">
            <el-icon><Edit /></el-icon>调整信用分
          </el-button>
        </div>
      </div>
    </div>

    <div class="page-card">
      <div class="card-header">
        <h3>信用变更记录</h3>
      </div>
      <el-table :data="tableData" v-loading="loading">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="userName" label="用户" width="120" />
        <el-table-column prop="changeType" label="变更类型" width="120">
          <template #default="{ row }">
            <el-tag size="small" :type="row.changeScore > 0 ? 'success' : 'danger'">
              {{ row.changeType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="changeScore" label="变动分值" width="100">
          <template #default="{ row }">
            <span :class="row.changeScore > 0 ? 'plus' : 'minus'">
              {{ row.changeScore > 0 ? '+' : '' }}{{ row.changeScore }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="beforeScore" label="变更前" width="100" />
        <el-table-column prop="afterScore" label="变更后" width="100" />
        <el-table-column prop="reason" label="变更原因" min-width="200" />
        <el-table-column prop="operatorName" label="操作人" width="100" />
        <el-table-column prop="createTime" label="变更时间" width="160" />
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

    <el-dialog v-model="showAdjust" title="调整用户信用分" width="550px">
      <el-form :model="adjustForm" label-width="100px">
        <el-form-item label="选择用户" prop="userId">
          <el-select v-model="adjustForm.userId" placeholder="请选择用户" style="width: 100%" filterable>
            <el-option v-for="u in userList" :key="u.id" :label="u.nickname || u.username" :value="u.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="调整分值" prop="changeScore">
          <el-input-number v-model="adjustForm.changeScore" :min="-100" :max="100" style="width: 100%" />
        </el-form-item>
        <el-form-item label="变更类型" prop="changeType">
          <el-select v-model="adjustForm.changeType" style="width: 100%">
            <el-option label="加分" value="加分" />
            <el-option label="扣分" value="扣分" />
          </el-select>
        </el-form-item>
        <el-form-item label="变更原因" prop="reason">
          <el-input v-model="adjustForm.reason" type="textarea" :rows="3" placeholder="请输入变更原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAdjust = false">取消</el-button>
        <el-button type="primary" @click="submitAdjust">确认调整</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, markRaw } from 'vue'
import { ElMessage } from 'element-plus'
import { UserFilled, InfoFilled, Edit } from '@element-plus/icons-vue'
import { api } from '@/api'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const loading = ref(false)
const total = ref(0)
const tableData = ref([])
const currentUser = ref(null)
const showAdjust = ref(false)
const userList = ref([])

const queryForm = reactive({
  pageNum: 1,
  pageSize: 10
})

const adjustForm = reactive({
  userId: null,
  changeScore: 0,
  changeType: '加分',
  reason: ''
})

const getCreditClass = (level) => {
  const map = {
    '优秀': 'level-excellent',
    '良好': 'level-good',
    '一般': 'level-average',
    '较差': 'level-poor',
    '很差': 'level-very-poor'
  }
  return map[level] || 'level-good'
}

const getProgressColor = (score) => {
  if (score >= 90) return '#67c23a'
  if (score >= 80) return '#409eff'
  if (score >= 70) return '#e6a23c'
  if (score >= 60) return '#f56c6c'
  return '#909399'
}

const loadData = async () => {
  loading.value = true
  try {
    const [userRes, recordRes] = await Promise.all([
      api.auth.getCurrentUser(),
      api.creditRating.page(queryForm)
    ])
    currentUser.value = userRes.data
    tableData.value = recordRes.data.records
    total.value = recordRes.data.total
  } catch (e) {
    console.error(e)
    currentUser.value = {
      nickname: '用户',
      creditScore: 85,
      creditLevel: '良好',
      publishCount: 12,
      exchangeCount: 8,
      claimCount: 15
    }
    tableData.value = [
      { id: 1, userName: '张三', changeType: '加分', changeScore: 5, beforeScore: 80, afterScore: 85, reason: '完成互换', operatorName: '系统', createTime: '2024-01-15 10:30:00' },
      { id: 2, userName: '张三', changeType: '加分', changeScore: 10, beforeScore: 70, afterScore: 80, reason: '首次发布物品', operatorName: '系统', createTime: '2024-01-10 14:20:00' }
    ]
    total.value = 2
  } finally {
    loading.value = false
  }
}

const handleSizeChange = () => {
  queryForm.pageNum = 1
  loadData()
}

const loadUsers = async () => {
  try {
    const res = await api.auth.listUsers()
    userList.value = res.data
  } catch (e) {
    userList.value = [
      { id: 1, nickname: '张三' },
      { id: 2, nickname: '李四' },
      { id: 3, nickname: '王五' }
    ]
  }
}

const submitAdjust = async () => {
  if (!adjustForm.userId || !adjustForm.reason) {
    ElMessage.warning('请填写完整信息')
    return
  }
  try {
    await api.creditRating.adjust(adjustForm)
    ElMessage.success('信用分调整成功')
    showAdjust.value = false
    loadData()
  } catch (e) {
    console.error(e)
  }
}

onMounted(() => {
  loadData()
  if (userStore.userInfo?.role === 'ADMIN') {
    loadUsers()
  }
})
</script>

<style scoped lang="scss">
.credit-header {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 20px;
  margin-bottom: 20px;
}

.credit-card {
  @include card-style;
  padding: 30px;

  .credit-info {
    display: flex;
    gap: 24px;
    margin-bottom: 24px;

    .credit-avatar {
      position: relative;
      .credit-level-badge {
        position: absolute;
        bottom: -6px;
        left: 50%;
        transform: translateX(-50%);
        padding: 2px 10px;
        border-radius: 10px;
        font-size: 11px;
        font-weight: 600;
        white-space: nowrap;
        &.level-excellent { background: #f0f9eb; color: #67c23a; }
        &.level-good { background: #ecf5ff; color: #409eff; }
        &.level-average { background: #fdf6ec; color: #e6a23c; }
        &.level-poor { background: #fef0f0; color: #f56c6c; }
        &.level-very-poor { background: #f4f4f5; color: #909399; }
      }
    }

    .credit-detail {
      flex: 1;
      h3 { font-size: 20px; font-weight: 600; margin-bottom: 12px; }
      .credit-score {
        display: flex;
        align-items: baseline;
        gap: 8px;
        margin-bottom: 16px;
        .score-number { font-size: 40px; font-weight: 700; color: $primary-color; line-height: 1; }
        .score-label { font-size: 14px; color: $text-secondary; }
      }
      .credit-stats {
        display: flex;
        gap: 40px;
        .stat {
          text-align: center;
          .num { display: block; font-size: 20px; font-weight: 600; color: $text-primary; }
          .label { font-size: 12px; color: $text-secondary; }
        }
      }
    }
  }

  .credit-progress {
    .progress-label {
      display: flex;
      justify-content: space-between;
      margin-bottom: 8px;
      font-size: 14px;
      color: $text-secondary;
    }
    .level-markers {
      display: flex;
      justify-content: space-between;
      margin-top: 8px;
      font-size: 12px;
      color: $text-secondary;
    }
  }
}

.credit-rules {
  @include card-style;
  padding: 24px;

  .rules-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 16px;
    font-weight: 600;
    margin-bottom: 16px;
    color: $primary-color;
  }

  .rules-list {
    list-style: none;
    padding: 0;
    margin: 0 0 20px;
    li {
      padding: 8px 0;
      display: flex;
      align-items: center;
      gap: 12px;
      font-size: 14px;
      color: $text-primary;
      border-bottom: 1px dashed $border-light;
      &:last-child { border-bottom: none; }
      .plus { color: $success-color; font-weight: 600; }
      .minus { color: $danger-color; font-weight: 600; }
    }
  }

  .admin-actions {
    .el-button { width: 100%; }
  }
}

.plus { color: $success-color; font-weight: 600; }
.minus { color: $danger-color; font-weight: 600; }

@media (max-width: 1200px) {
  .credit-header {
    grid-template-columns: 1fr;
  }
}
</style>
