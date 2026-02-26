<template>
  <div class="ai-management">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-value">{{ summary.totalCallCount?.toLocaleString() || 0 }}</div>
            <div class="stat-label">总调用次数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-value cost">{{ summary.totalCost?.toFixed(2) || '0.00' }}</div>
            <div class="stat-label">总费用 (元)</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-value">{{ summary.todayCallCount?.toLocaleString() || 0 }}</div>
            <div class="stat-label">今日调用</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-value cost">{{ summary.monthCost?.toFixed(2) || '0.00' }}</div>
            <div class="stat-label">本月费用 (元)</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 选项卡 -->
    <el-tabs v-model="activeTab" class="main-tabs">
      <!-- AI模型配置 -->
      <el-tab-pane label="AI模型配置" name="models">
        <div class="toolbar">
          <el-button type="primary" @click="showModelDialog()">
            <el-icon><Plus /></el-icon>
            新增模型
          </el-button>
        </div>

        <el-table :data="modelList" v-loading="modelLoading" stripe>
          <el-table-column prop="providerName" label="服务商" width="120" />
          <el-table-column prop="modelName" label="模型名称" width="180" />
          <el-table-column prop="apiEndpoint" label="API端点" min-width="200" show-overflow-tooltip />
          <el-table-column label="价格(元/千token)" width="160">
            <template #default="{ row }">
              <div>输入: {{ row.inputPrice?.toFixed(4) || '-' }}</div>
              <div>输出: {{ row.outputPrice?.toFixed(4) || '-' }}</div>
            </template>
          </el-table-column>
          <el-table-column label="默认" width="80" align="center">
            <template #default="{ row }">
              <el-tag v-if="row.isDefault === 1" type="success" size="small">默认</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
                {{ row.status === 1 ? '启用' : '停用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" size="small" @click="showModelDialog(row)">编辑</el-button>
              <el-button link type="primary" size="small" @click="setDefault(row)" :disabled="row.isDefault === 1 || row.status !== 1">设为默认</el-button>
              <el-button link type="danger" size="small" @click="deleteModel(row)" :disabled="row.isDefault === 1">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 调用统计 -->
      <el-tab-pane label="调用统计" name="usage">
        <div class="toolbar">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            @change="fetchUsageDaily"
          />
        </div>

        <el-table :data="usageList" v-loading="usageLoading" stripe>
          <el-table-column prop="statDate" label="日期" width="120" />
          <el-table-column prop="providerName" label="服务商" width="120" />
          <el-table-column prop="modelName" label="模型" width="160" />
          <el-table-column prop="callCount" label="调用次数" width="100" align="right">
            <template #default="{ row }">{{ row.callCount?.toLocaleString() }}</template>
          </el-table-column>
          <el-table-column label="输入Token" width="120" align="right">
            <template #default="{ row }">{{ row.totalInputTokens?.toLocaleString() }}</template>
          </el-table-column>
          <el-table-column label="输出Token" width="120" align="right">
            <template #default="{ row }">{{ row.totalOutputTokens?.toLocaleString() }}</template>
          </el-table-column>
          <el-table-column label="费用(元)" width="100" align="right">
            <template #default="{ row }">
              <span class="cost-text">{{ row.totalCost?.toFixed(4) }}</span>
            </template>
          </el-table-column>
        </el-table>

        <el-pagination
          v-model:current-page="usagePage"
          v-model:page-size="usageSize"
          :total="usageTotal"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @change="fetchUsageDaily"
          class="pagination"
        />
      </el-tab-pane>
    </el-tabs>

    <!-- 模型编辑弹窗 -->
    <el-dialog
      v-model="modelDialogVisible"
      :title="editingModel ? '编辑AI模型' : '新增AI模型'"
      width="600px"
    >
      <el-form ref="modelFormRef" :model="modelForm" :rules="modelRules" label-width="100px">
        <el-form-item label="服务商" prop="providerCode">
          <el-select v-model="modelForm.providerCode" placeholder="选择服务商" @change="onProviderChange">
            <el-option label="OpenAI" value="openai" />
            <el-option label="Claude" value="claude" />
            <el-option label="通义千问" value="qwen" />
            <el-option label="DeepSeek" value="deepseek" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="服务商名称" prop="providerName">
          <el-input v-model="modelForm.providerName" placeholder="显示名称" />
        </el-form-item>
        <el-form-item label="模型名称" prop="modelName">
          <el-input v-model="modelForm.modelName" placeholder="如 gpt-4, qwen-turbo" />
        </el-form-item>
        <el-form-item label="API端点">
          <el-input v-model="modelForm.apiEndpoint" placeholder="留空使用默认端点" />
        </el-form-item>
        <el-form-item label="API Key" prop="apiKey">
          <el-input v-model="modelForm.apiKey" type="password" show-password :placeholder="editingModel ? '留空不修改' : '请输入API Key'" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="输入价格">
              <el-input-number v-model="modelForm.inputPrice" :precision="6" :min="0" :step="0.0001" style="width: 100%;" />
              <div class="form-tip">元/千token</div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="输出价格">
              <el-input-number v-model="modelForm.outputPrice" :precision="6" :min="0" :step="0.0001" style="width: 100%;" />
              <div class="form-tip">元/千token</div>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="最大Token">
              <el-input-number v-model="modelForm.maxTokens" :min="512" :max="128000" :step="512" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="温度">
              <el-input-number v-model="modelForm.temperature" :precision="2" :min="0" :max="2" :step="0.1" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="modelForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="modelDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitModelForm" :loading="modelSubmitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import {
  getAiModelConfigs,
  createAiModelConfig,
  updateAiModelConfig,
  deleteAiModelConfig,
  setDefaultAiModel,
  getAiUsageDaily,
  getAiUsageSummary,
  type AiModelConfigItem,
  type AiUsageDailyItem,
  type AiUsageSummary
} from '@/api/admin'

// 统计数据
const summary = ref<AiUsageSummary>({
  totalCallCount: 0,
  totalInputTokens: 0,
  totalOutputTokens: 0,
  totalCost: 0,
  todayCallCount: 0,
  todayCost: 0,
  monthCallCount: 0,
  monthCost: 0
})

const activeTab = ref('models')

// 模型列表
const modelList = ref<AiModelConfigItem[]>([])
const modelLoading = ref(false)

// 调用统计
const usageList = ref<AiUsageDailyItem[]>([])
const usageLoading = ref(false)
const usagePage = ref(1)
const usageSize = ref(20)
const usageTotal = ref(0)
const dateRange = ref<[string, string] | null>(null)

// 模型编辑
const modelDialogVisible = ref(false)
const editingModel = ref<AiModelConfigItem | null>(null)
const modelFormRef = ref<FormInstance>()
const modelSubmitting = ref(false)
const modelForm = reactive({
  providerCode: '',
  providerName: '',
  modelName: '',
  apiEndpoint: '',
  apiKey: '',
  inputPrice: 0,
  outputPrice: 0,
  maxTokens: 4096,
  temperature: 0.7,
  status: 1
})

const modelRules: FormRules = {
  providerCode: [{ required: true, message: '请选择服务商', trigger: 'change' }],
  providerName: [{ required: true, message: '请输入服务商名称', trigger: 'blur' }],
  modelName: [{ required: true, message: '请输入模型名称', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const providerNameMap: Record<string, string> = {
  openai: 'OpenAI',
  claude: 'Anthropic Claude',
  qwen: '通义千问',
  deepseek: 'DeepSeek',
  other: ''
}

const onProviderChange = (value: string) => {
  if (providerNameMap[value]) {
    modelForm.providerName = providerNameMap[value]
  }
}

const fetchSummary = async () => {
  try {
    const res = await getAiUsageSummary()
    summary.value = res.data || {}
  } catch (e) {
    console.error('获取AI统计失败', e)
  }
}

const fetchModels = async () => {
  modelLoading.value = true
  try {
    const res = await getAiModelConfigs({ page: 1, size: 100 })
    modelList.value = res.data?.records || []
  } catch (e) {
    console.error('获取模型列表失败', e)
  } finally {
    modelLoading.value = false
  }
}

const fetchUsageDaily = async () => {
  usageLoading.value = true
  try {
    const params: any = {
      page: usagePage.value,
      size: usageSize.value
    }
    if (dateRange.value) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }
    const res = await getAiUsageDaily(params)
    usageList.value = res.data?.records || []
    usageTotal.value = res.data?.total || 0
  } catch (e) {
    console.error('获取调用统计失败', e)
  } finally {
    usageLoading.value = false
  }
}

const showModelDialog = (model?: AiModelConfigItem) => {
  editingModel.value = model || null
  if (model) {
    Object.assign(modelForm, {
      providerCode: model.providerCode,
      providerName: model.providerName,
      modelName: model.modelName,
      apiEndpoint: model.apiEndpoint || '',
      apiKey: '',
      inputPrice: model.inputPrice || 0,
      outputPrice: model.outputPrice || 0,
      maxTokens: model.maxTokens || 4096,
      temperature: model.temperature || 0.7,
      status: model.status
    })
  } else {
    Object.assign(modelForm, {
      providerCode: '',
      providerName: '',
      modelName: '',
      apiEndpoint: '',
      apiKey: '',
      inputPrice: 0,
      outputPrice: 0,
      maxTokens: 4096,
      temperature: 0.7,
      status: 1
    })
  }
  modelDialogVisible.value = true
}

const submitModelForm = async () => {
  const valid = await modelFormRef.value?.validate().catch(() => false)
  if (!valid) return

  modelSubmitting.value = true
  try {
    const data = { ...modelForm }
    if (editingModel.value) {
      await updateAiModelConfig(editingModel.value.configId, data)
      ElMessage.success('更新成功')
    } else {
      if (!data.apiKey) {
        ElMessage.error('请输入API Key')
        return
      }
      await createAiModelConfig(data)
      ElMessage.success('创建成功')
    }
    modelDialogVisible.value = false
    fetchModels()
  } catch (e) {
    ElMessage.error('操作失败')
  } finally {
    modelSubmitting.value = false
  }
}

const setDefault = async (model: AiModelConfigItem) => {
  try {
    await setDefaultAiModel(model.configId)
    ElMessage.success('设置成功')
    fetchModels()
  } catch (e) {
    ElMessage.error('设置失败')
  }
}

const deleteModel = async (model: AiModelConfigItem) => {
  try {
    await ElMessageBox.confirm('确定要删除该模型配置吗？', '确认删除', {
      type: 'warning'
    })
    await deleteAiModelConfig(model.configId)
    ElMessage.success('删除成功')
    fetchModels()
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  fetchSummary()
  fetchModels()
  fetchUsageDaily()
})
</script>

<style scoped>
.ai-management {
  padding: 20px;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  text-align: center;
}

.stat-content {
  padding: 10px 0;
}

.stat-value {
  font-size: 32px;
  font-weight: 600;
  color: #333;
}

.stat-value.cost {
  color: #e6a23c;
}

.stat-label {
  font-size: 14px;
  color: #999;
  margin-top: 8px;
}

.main-tabs {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
}

.toolbar {
  margin-bottom: 16px;
  display: flex;
  gap: 16px;
}

.pagination {
  margin-top: 20px;
  justify-content: flex-end;
}

.cost-text {
  color: #e6a23c;
  font-weight: 500;
}

.form-tip {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}
</style>
