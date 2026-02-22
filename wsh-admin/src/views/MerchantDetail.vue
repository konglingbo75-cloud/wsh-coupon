<template>
  <div class="merchant-detail" v-loading="loading">
    <el-page-header @back="router.back()" :content="detail?.merchantName || '商户详情'" />

    <el-row :gutter="20" style="margin-top: 20px" v-if="detail">
      <el-col :span="16">
        <el-card shadow="hover">
          <template #header>基本信息</template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="商户编码">{{ detail.merchantCode }}</el-descriptions-item>
            <el-descriptions-item label="商户名称">{{ detail.merchantName }}</el-descriptions-item>
            <el-descriptions-item label="联系人">{{ detail.contactName }}</el-descriptions-item>
            <el-descriptions-item label="联系电话">{{ detail.contactPhone }}</el-descriptions-item>
            <el-descriptions-item label="城市">{{ detail.city }}</el-descriptions-item>
            <el-descriptions-item label="经营类目">{{ detail.businessCategory }}</el-descriptions-item>
            <el-descriptions-item label="地址" :span="2">{{ detail.address }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="statusTag(detail.status).type">{{ statusTag(detail.status).text }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="对接类型">{{ integrationText(detail.integrationType) }}</el-descriptions-item>
            <el-descriptions-item label="服务费率">{{ ((detail.profitSharingRate || 0) * 100).toFixed(2) }}%</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ detail.createdAt }}</el-descriptions-item>
          </el-descriptions>
        </el-card>

        <el-card shadow="hover" style="margin-top: 20px">
          <template #header>门店列表</template>
          <el-table :data="detail.branches" stripe>
            <el-table-column prop="branchId" label="ID" width="80" />
            <el-table-column prop="branchName" label="门店名称" />
            <el-table-column prop="address" label="地址" />
            <el-table-column label="状态" width="80" align="center">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '正常' : '停用' }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card shadow="hover" v-if="detail.onboardingFee">
          <template #header>入驻费信息</template>
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item label="套餐">{{ detail.onboardingFee.planName }}</el-descriptions-item>
            <el-descriptions-item label="金额">&yen;{{ detail.onboardingFee.feeAmount }}</el-descriptions-item>
            <el-descriptions-item label="支付状态">
              <el-tag :type="detail.onboardingFee.payStatus === 1 ? 'success' : 'warning'">
                {{ detail.onboardingFee.payStatus === 1 ? '已支付' : '待支付' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="有效期" v-if="detail.onboardingFee.validStartDate">
              {{ detail.onboardingFee.validStartDate }} ~ {{ detail.onboardingFee.validEndDate }}
            </el-descriptions-item>
          </el-descriptions>
        </el-card>

        <el-card shadow="hover" :style="{ marginTop: detail.onboardingFee ? '20px' : '0' }">
          <template #header>审核日志</template>
          <el-timeline v-if="detail.auditLogs.length">
            <el-timeline-item
              v-for="log in detail.auditLogs"
              :key="log.logId"
              :timestamp="log.createdAt"
              placement="top"
              :type="log.action === 'APPROVE' ? 'success' : log.action === 'REJECT' ? 'danger' : 'warning'"
            >
              <p><strong>{{ actionText(log.action) }}</strong> - {{ log.adminName }}</p>
              <p v-if="log.reason" style="color: #909399; font-size: 13px">{{ log.reason }}</p>
            </el-timeline-item>
          </el-timeline>
          <el-empty v-else description="暂无审核日志" :image-size="60" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getMerchantDetail, type MerchantDetail } from '@/api/admin'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const detail = ref<MerchantDetail | null>(null)

function statusTag(status: number) {
  const map: Record<number, { type: 'warning' | 'success' | 'danger'; text: string }> = {
    0: { type: 'warning', text: '待审核' },
    1: { type: 'success', text: '正常' },
    2: { type: 'danger', text: '冻结' }
  }
  return map[status] || { type: 'warning', text: '未知' }
}

function integrationText(type: number) {
  const map: Record<number, string> = { 1: 'API对接', 2: 'RPA对接', 3: '手动管理' }
  return map[type] || '未知'
}

function actionText(action: string) {
  const map: Record<string, string> = { APPROVE: '审核通过', REJECT: '审核拒绝', FREEZE: '冻结', UNFREEZE: '解冻' }
  return map[action] || action
}

onMounted(async () => {
  loading.value = true
  try {
    const id = Number(route.params.id)
    const res = await getMerchantDetail(id)
    detail.value = res.data
  } catch { /* */ } finally {
    loading.value = false
  }
})
</script>
