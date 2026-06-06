<template>
  <div class="page-container">
    <div class="page-header">
      <h2>发布闲置物品</h2>
      <el-button @click="router.back()">
        <el-icon><ArrowLeft /></el-icon>返回
      </el-button>
    </div>

    <div class="page-card">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="物品名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入物品名称" size="large" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="物品分类" prop="category">
              <el-select v-model="form.category" placeholder="请选择分类" size="large" style="width: 100%">
                <el-option v-for="cat in categories" :key="cat.value" :label="cat.label" :value="cat.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="成色" prop="itemCondition">
              <el-select v-model="form.itemCondition" placeholder="请选择成色" size="large" style="width: 100%">
                <el-option v-for="c in conditionOptions" :key="c.value" :label="c.label" :value="c.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="数量" prop="quantity">
              <el-input-number v-model="form.quantity" :min="1" :max="999" size="large" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="自提点" prop="pickupPointId">
              <el-select v-model="form.pickupPointId" placeholder="请选择自提点" size="large" style="width: 100%">
                <el-option v-for="p in pickupPoints" :key="p.id" :label="p.name" :value="p.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="期望交换">
              <el-input v-model="form.expectedExchange" placeholder="期望交换的物品（选填）" size="large" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="物品描述" prop="description">
              <el-input v-model="form.description" type="textarea" :rows="4" placeholder="请详细描述物品情况" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="物品图片">
              <el-upload
                action="#"
                list-type="picture-card"
                :auto-upload="false"
                :limit="5"
                :on-preview="handlePreview"
                :on-remove="handleRemove"
                :on-change="handleChange"
              >
                <el-icon><Plus /></el-icon>
              </el-upload>
              <div class="tip">最多上传5张图片，支持JPG、PNG格式</div>
            </el-form-item>
          </el-col>
        </el-row>
        <div class="form-actions">
          <el-button size="large" @click="router.back()">取消</el-button>
          <el-button type="primary" size="large" :loading="loading" @click="handleSubmit">发布物品</el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { api } from '@/api'

const router = useRouter()
const formRef = ref()
const loading = ref(false)
const pickupPoints = ref([])
const fileList = ref([])

const form = reactive({
  name: '',
  category: '',
  itemCondition: '',
  quantity: 1,
  pickupPointId: null,
  expectedExchange: '',
  description: '',
  imageUrl: ''
})

const categories = [
  { label: '书籍', value: '书籍' },
  { label: '家居', value: '家居' },
  { label: '电子', value: '电子' },
  { label: '衣物', value: '衣物' },
  { label: '玩具', value: '玩具' },
  { label: '其他', value: '其他' }
]

const conditionOptions = [
  { label: '全新', value: '全新' },
  { label: '九成新', value: '九成新' },
  { label: '八成新', value: '八成新' },
  { label: '七成新', value: '七成新' },
  { label: '六成新', value: '六成新' }
]

const rules = {
  name: [{ required: true, message: '请输入物品名称', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  itemCondition: [{ required: true, message: '请选择成色', trigger: 'change' }],
  quantity: [{ required: true, message: '请输入数量', trigger: 'blur' }],
  pickupPointId: [{ required: true, message: '请选择自提点', trigger: 'change' }],
  description: [{ required: true, message: '请输入物品描述', trigger: 'blur' }]
}

const loadPickupPoints = async () => {
  try {
    const res = await api.pickupPoint.list()
    pickupPoints.value = res.data
  } catch (e) {
    console.error(e)
  }
}

const handlePreview = () => {}
const handleRemove = () => {}
const handleChange = () => {}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    loading.value = true
    form.imageUrl = 'https://placeholder-pic.jpg'
    await api.idleItem.publish(form)
    ElMessage.success('发布成功')
    router.push('/idle-item')
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadPickupPoints()
})
</script>

<style scoped lang="scss">
.tip {
  font-size: 12px;
  color: $text-secondary;
  margin-top: 8px;
}
.form-actions {
  text-align: center;
  margin-top: 20px;
}
</style>
