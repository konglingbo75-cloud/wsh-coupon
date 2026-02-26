<template>
  <div class="billing-page">
    <!-- Tab 切换 -->
    <el-tabs v-model="activeTab" @tab-change="onTabChange">
      <el-tab-pane label="入驻费" name="onboarding" />
      <el-tab-pane label="套餐购买" name="packages" />
      <el-tab-pane label="保证金" name="deposits" />
      <el-tab-pane label="服务费" name="serviceFees" />
      <el-tab-pane label="套餐管理" name="packageManage" />
      <el-tab-pane label="入驻费套餐" name="onboardingPlanManage" />
    </el-tabs>

    <!-- Tab: 入驻费 -->
    <el-card v-if="activeTab === 'onboarding'" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>入驻费记录</span>
          <div class="header-actions">
            <el-select v-model="onboardingQuery.status" placeholder="支付状态" clearable style="width: 140px" @change="fetchOnboarding">
              <el-option label="待支付" :value="0" />
              <el-option label="已支付" :value="1" />
              <el-option label="已关闭" :value="2" />
            </el-select>
            <el-button type="primary" @click="fetchOnboarding">搜索</el-button>
          </div>
        </div>
      </template>

      <el-table :data="onboardingData" v-loading="onboardingLoading" stripe>
        <el-table-column prop="merchantName" label="商户名称" min-width="140" />
        <el-table-column prop="planName" label="套餐" width="130" />
        <el-table-column label="金额" width="100" align="right">
          <template #default="{ row }">&yen;{{ row.feeAmount?.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="支付状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="payStatusType(row.payStatus)">{{ payStatusText(row.payStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="payTime" label="支付时间" width="170" />
        <el-table-column label="有效期" width="200">
          <template #default="{ row }">
            <span v-if="row.validStartDate">{{ row.validStartDate }} ~ {{ row.validEndDate }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170" />
      </el-table>

      <el-pagination
        class="pagination"
        v-model:current-page="onboardingQuery.page"
        v-model:page-size="onboardingQuery.size"
        :total="onboardingTotal"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="fetchOnboarding"
        @current-change="fetchOnboarding"
      />
    </el-card>

    <!-- Tab: 套餐购买 -->
    <el-card v-if="activeTab === 'packages'" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>套餐购买记录</span>
          <div class="header-actions">
            <el-select v-model="packageQuery.status" placeholder="支付状态" clearable style="width: 140px" @change="fetchPackages">
              <el-option label="待支付" :value="0" />
              <el-option label="已支付" :value="1" />
              <el-option label="已关闭" :value="2" />
            </el-select>
            <el-button type="primary" @click="fetchPackages">搜索</el-button>
          </div>
        </div>
      </template>

      <el-table :data="packageData" v-loading="packageLoading" stripe>
        <el-table-column prop="merchantName" label="商户名称" min-width="140" />
        <el-table-column prop="packageName" label="套餐名称" width="140" />
        <el-table-column label="实付金额" width="100" align="right">
          <template #default="{ row }">&yen;{{ row.pricePaid?.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="支付状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="payStatusType(row.payStatus)">{{ payStatusText(row.payStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="payTime" label="支付时间" width="170" />
        <el-table-column label="有效期" width="200">
          <template #default="{ row }">
            <span v-if="row.validStartDate">{{ row.validStartDate }} ~ {{ row.validEndDate }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170" />
      </el-table>

      <el-pagination
        class="pagination"
        v-model:current-page="packageQuery.page"
        v-model:page-size="packageQuery.size"
        :total="packageTotal"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="fetchPackages"
        @current-change="fetchPackages"
      />
    </el-card>

    <!-- Tab: 保证金 -->
    <el-card v-if="activeTab === 'deposits'" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>保证金记录</span>
          <div class="header-actions">
            <el-select v-model="depositQuery.status" placeholder="支付状态" clearable style="width: 140px" @change="fetchDeposits">
              <el-option label="待缴纳" :value="0" />
              <el-option label="已缴纳" :value="1" />
              <el-option label="退还中" :value="2" />
              <el-option label="已退还" :value="3" />
            </el-select>
            <el-button type="primary" @click="fetchDeposits">搜索</el-button>
          </div>
        </div>
      </template>

      <el-table :data="depositData" v-loading="depositLoading" stripe>
        <el-table-column prop="merchantName" label="商户名称" min-width="140" />
        <el-table-column label="保证金金额" width="120" align="right">
          <template #default="{ row }">&yen;{{ row.depositAmount?.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="depositStatusType(row.payStatus)">{{ depositStatusText(row.payStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="payTime" label="缴纳时间" width="170" />
        <el-table-column prop="refundTime" label="退款时间" width="170" />
        <el-table-column prop="refundReason" label="退款原因" min-width="120" />
        <el-table-column prop="createdAt" label="创建时间" width="170" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.payStatus === 2" type="primary" link @click="showRefundDialog(row)">审核</el-button>
            <span v-else>-</span>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        class="pagination"
        v-model:current-page="depositQuery.page"
        v-model:page-size="depositQuery.size"
        :total="depositTotal"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="fetchDeposits"
        @current-change="fetchDeposits"
      />
    </el-card>

    <!-- Tab: 服务费 -->
    <el-card v-if="activeTab === 'serviceFees'" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>月度服务费汇总</span>
          <div class="header-actions">
            <el-select v-model="feeQuery.status" placeholder="扣减状态" clearable style="width: 140px" @change="fetchServiceFees">
              <el-option label="待扣减" :value="0" />
              <el-option label="已扣减" :value="1" />
              <el-option label="余额不足" :value="2" />
            </el-select>
            <el-button type="primary" @click="fetchServiceFees">搜索</el-button>
          </div>
        </div>
      </template>

      <el-table :data="feeData" v-loading="feeLoading" stripe>
        <el-table-column prop="merchantName" label="商户名称" min-width="140" />
        <el-table-column prop="yearMonth" label="月份" width="100" />
        <el-table-column prop="orderCount" label="订单数" width="90" align="right" />
        <el-table-column label="订单总额" width="120" align="right">
          <template #default="{ row }">&yen;{{ row.totalAmount?.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="服务费" width="120" align="right">
          <template #default="{ row }">
            <span style="color: #ff6b35; font-weight: bold">&yen;{{ row.serviceFee?.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="扣减状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="deductStatusType(row.deductStatus)">{{ deductStatusText(row.deductStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="deductTime" label="扣减时间" width="170" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.deductStatus !== 1" type="primary" link @click="handleManualDeduct(row)">手动扣减</el-button>
            <el-button type="warning" link @click="showFeeAdjustDialog(row)">调整状态</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        class="pagination"
        v-model:current-page="feeQuery.page"
        v-model:page-size="feeQuery.size"
        :total="feeTotal"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="fetchServiceFees"
        @current-change="fetchServiceFees"
      />
    </el-card>

    <!-- Tab: 套餐管理 -->
    <el-card v-if="activeTab === 'packageManage'" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>套餐管理</span>
          <div class="header-actions">
            <el-select v-model="pkgManageQuery.status" placeholder="状态" clearable style="width: 120px" @change="fetchPackageManage">
              <el-option label="启用" :value="1" />
              <el-option label="停用" :value="0" />
            </el-select>
            <el-button type="primary" @click="showCreateDialog">新增套餐</el-button>
          </div>
        </div>
      </template>

      <el-table :data="pkgManageData" v-loading="pkgManageLoading" stripe>
        <el-table-column prop="packageName" label="套餐名称" min-width="120" />
        <el-table-column label="类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="packageTypeTag(row.packageType)">{{ packageTypeText(row.packageType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="价格" width="100" align="right">
          <template #default="{ row }">&yen;{{ row.price?.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="durationMonths" label="有效期(月)" width="100" align="center" />
        <el-table-column label="服务费率" width="100" align="center">
          <template #default="{ row }">{{ (row.serviceFeeRate * 100).toFixed(1) }}%</template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="80" align="center" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status === 1"
              @change="(val: boolean) => handleStatusChange(row.packageId, val)"
              inline-prompt
              active-text="启用"
              inactive-text="停用"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170" />
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="showEditDialog(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        class="pagination"
        v-model:current-page="pkgManageQuery.page"
        v-model:page-size="pkgManageQuery.size"
        :total="pkgManageTotal"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="fetchPackageManage"
        @current-change="fetchPackageManage"
      />
    </el-card>

    <!-- 套餐编辑弹窗 -->
    <el-dialog v-model="pkgDialogVisible" :title="pkgFormMode === 'create' ? '新增套餐' : '编辑套餐'" width="500px">
      <el-form ref="pkgFormRef" :model="pkgForm" :rules="pkgFormRules" label-width="100px">
        <el-form-item label="套餐名称" prop="packageName">
          <el-input v-model="pkgForm.packageName" placeholder="请输入套餐名称" maxlength="64" />
        </el-form-item>
        <el-form-item label="套餐类型" prop="packageType">
          <el-select v-model="pkgForm.packageType" placeholder="请选择套餐类型" style="width: 100%">
            <el-option label="基础版" :value="1" />
            <el-option label="专业版" :value="2" />
            <el-option label="旗舰版" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="pkgForm.price" :min="0.01" :precision="2" :step="10" style="width: 100%" />
        </el-form-item>
        <el-form-item label="有效期(月)" prop="durationMonths">
          <el-input-number v-model="pkgForm.durationMonths" :min="1" :max="36" :step="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="服务费率(%)" prop="serviceFeeRatePercent">
          <el-input-number v-model="pkgForm.serviceFeeRatePercent" :min="0" :max="100" :precision="1" :step="0.5" style="width: 100%" />
        </el-form-item>
        <el-form-item label="套餐特性" prop="features">
          <el-input v-model="pkgForm.features" type="textarea" :rows="3" placeholder="套餐特性说明(可选)" maxlength="500" />
        </el-form-item>
        <el-form-item label="排序权重" prop="sortOrder">
          <el-input-number v-model="pkgForm.sortOrder" :min="0" :max="999" :step="1" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pkgDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="pkgSubmitting" @click="handlePkgSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- Tab: 入驻费套餐管理 -->
    <el-card v-if="activeTab === 'onboardingPlanManage'" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>入驻费套餐管理</span>
          <div class="header-actions">
            <el-select v-model="planManageQuery.status" placeholder="状态" clearable style="width: 120px" @change="fetchOnboardingPlanManage">
              <el-option label="启用" :value="1" />
              <el-option label="停用" :value="0" />
            </el-select>
            <el-button type="primary" @click="showPlanCreateDialog">新增套餐</el-button>
          </div>
        </div>
      </template>

      <el-table :data="planManageData" v-loading="planManageLoading" stripe>
        <el-table-column prop="planName" label="套餐名称" min-width="140" />
        <el-table-column label="类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.planType === 1 ? '' : 'success'">{{ planTypeText(row.planType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="费用" width="120" align="right">
          <template #default="{ row }">&yen;{{ row.feeAmount?.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="durationMonths" label="有效期(月)" width="100" align="center" />
        <el-table-column prop="description" label="描述" min-width="150" show-overflow-tooltip />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status === 1"
              @change="(val: boolean) => handlePlanStatusChange(row.planId, val)"
              inline-prompt
              active-text="启用"
              inactive-text="停用"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170" />
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="showPlanEditDialog(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        class="pagination"
        v-model:current-page="planManageQuery.page"
        v-model:page-size="planManageQuery.size"
        :total="planManageTotal"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="fetchOnboardingPlanManage"
        @current-change="fetchOnboardingPlanManage"
      />
    </el-card>

    <!-- 入驻费套餐编辑弹窗 -->
    <el-dialog v-model="planDialogVisible" :title="planFormMode === 'create' ? '新增入驻费套餐' : '编辑入驻费套餐'" width="500px">
      <el-form ref="planFormRef" :model="planForm" :rules="planFormRules" label-width="100px">
        <el-form-item label="套餐名称" prop="planName">
          <el-input v-model="planForm.planName" placeholder="请输入套餐名称" maxlength="64" />
        </el-form-item>
        <el-form-item label="套餐类型" prop="planType">
          <el-select v-model="planForm.planType" placeholder="请选择套餐类型" style="width: 100%">
            <el-option label="按门店" :value="1" />
            <el-option label="按品牌" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="费用金额" prop="feeAmount">
          <el-input-number v-model="planForm.feeAmount" :min="0.01" :precision="2" :step="100" style="width: 100%" />
        </el-form-item>
        <el-form-item label="有效期(月)" prop="durationMonths">
          <el-input-number v-model="planForm.durationMonths" :min="1" :max="60" :step="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="planForm.description" type="textarea" :rows="3" placeholder="套餐描述(可选)" maxlength="500" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="planDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="planSubmitting" @click="handlePlanSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 保证金退款审核弹窗 -->
    <el-dialog v-model="refundDialogVisible" title="保证金退款审核" width="450px">
      <div style="margin-bottom: 16px">
        <p><strong>商户：</strong>{{ refundingDeposit?.merchantName }}</p>
        <p><strong>保证金金额：</strong>&yen;{{ refundingDeposit?.depositAmount?.toFixed(2) }}</p>
      </div>
      <el-form label-width="80px">
        <el-form-item label="审核结果">
          <el-radio-group v-model="refundAction">
            <el-radio value="approve">通过退款</el-radio>
            <el-radio value="reject">拒绝退款</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="refundAction === 'reject'" label="拒绝原因">
          <el-input v-model="refundReason" type="textarea" :rows="2" placeholder="请输入拒绝原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="refundDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="refundSubmitting" @click="handleRefundSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 服务费状态调整弹窗 -->
    <el-dialog v-model="feeAdjustDialogVisible" title="调整服务费状态" width="450px">
      <el-alert type="warning" :closable="false" style="margin-bottom: 16px">
        状态调整会影响财务数据，请谨慎操作！
      </el-alert>
      <div style="margin-bottom: 16px">
        <p><strong>商户：</strong>{{ adjustingFee?.merchantName }}</p>
        <p><strong>月份：</strong>{{ adjustingFee?.yearMonth }}</p>
        <p><strong>服务费：</strong>&yen;{{ adjustingFee?.serviceFee?.toFixed(2) }}</p>
        <p><strong>当前状态：</strong>{{ deductStatusText(adjustingFee?.deductStatus || 0) }}</p>
      </div>
      <el-form label-width="80px">
        <el-form-item label="新状态">
          <el-select v-model="feeNewStatus" style="width: 100%">
            <el-option label="待扣减" :value="0" />
            <el-option label="已扣减" :value="1" />
            <el-option label="余额不足" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="调整原因">
          <el-input v-model="feeAdjustReason" type="textarea" :rows="2" placeholder="请输入调整原因（必填）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="feeAdjustDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="feeAdjustSubmitting" @click="handleFeeAdjustSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  getBillings, type BillingItem,
  getPackagePurchases, type PackagePurchaseItem,
  getDeposits, type DepositItem,
  getServiceFeeSummaries, type ServiceFeeSummaryItem,
  getPackageManage, type PackageManageItem,
  createPackage, updatePackage, updatePackageStatus,
  getOnboardingPlanManage, type OnboardingPlanManageItem,
  createOnboardingPlan, updateOnboardingPlan, updateOnboardingPlanStatus,
  handleDepositRefund, manualDeductServiceFee, adjustServiceFeeStatus
} from '@/api/admin'

const activeTab = ref('onboarding')

// ========== 共用工具 ==========
function payStatusText(s: number) {
  const map: Record<number, string> = { 0: '待支付', 1: '已支付', 2: '已关闭' }
  return map[s] || '未知'
}
function payStatusType(s: number): 'warning' | 'success' | 'info' {
  const map: Record<number, 'warning' | 'success' | 'info'> = { 0: 'warning', 1: 'success', 2: 'info' }
  return map[s] || 'info'
}
function depositStatusText(s: number) {
  const map: Record<number, string> = { 0: '待缴纳', 1: '已缴纳', 2: '退还中', 3: '已退还' }
  return map[s] || '未知'
}
function depositStatusType(s: number): 'warning' | 'success' | 'info' | 'danger' {
  const map: Record<number, 'warning' | 'success' | 'info' | 'danger'> = { 0: 'warning', 1: 'success', 2: 'info', 3: 'info' }
  return map[s] || 'info'
}
function deductStatusText(s: number) {
  const map: Record<number, string> = { 0: '待扣减', 1: '已扣减', 2: '余额不足' }
  return map[s] || '未知'
}
function deductStatusType(s: number): 'warning' | 'success' | 'danger' {
  const map: Record<number, 'warning' | 'success' | 'danger'> = { 0: 'warning', 1: 'success', 2: 'danger' }
  return map[s] || 'warning'
}
function planTypeText(t: number) {
  const map: Record<number, string> = { 1: '按门店', 2: '按品牌' }
  return map[t] || '未知'
}

// ========== 入驻费 ==========
const onboardingLoading = ref(false)
const onboardingData = ref<BillingItem[]>([])
const onboardingTotal = ref(0)
const onboardingQuery = reactive({ page: 1, size: 20, status: null as number | null })

async function fetchOnboarding() {
  onboardingLoading.value = true
  try {
    const res = await getBillings(onboardingQuery)
    onboardingData.value = res.data.records
    onboardingTotal.value = res.data.total
  } catch { /* */ } finally {
    onboardingLoading.value = false
  }
}

// ========== 套餐购买 ==========
const packageLoading = ref(false)
const packageData = ref<PackagePurchaseItem[]>([])
const packageTotal = ref(0)
const packageQuery = reactive({ page: 1, size: 20, status: null as number | null })

async function fetchPackages() {
  packageLoading.value = true
  try {
    const res = await getPackagePurchases(packageQuery)
    packageData.value = res.data.records
    packageTotal.value = res.data.total
  } catch { /* */ } finally {
    packageLoading.value = false
  }
}

// ========== 保证金 ==========
const depositLoading = ref(false)
const depositData = ref<DepositItem[]>([])
const depositTotal = ref(0)
const depositQuery = reactive({ page: 1, size: 20, status: null as number | null })

async function fetchDeposits() {
  depositLoading.value = true
  try {
    const res = await getDeposits(depositQuery)
    depositData.value = res.data.records
    depositTotal.value = res.data.total
  } catch { /* */ } finally {
    depositLoading.value = false
  }
}

// ========== 服务费 ==========
const feeLoading = ref(false)
const feeData = ref<ServiceFeeSummaryItem[]>([])
const feeTotal = ref(0)
const feeQuery = reactive({ page: 1, size: 20, status: null as number | null })

async function fetchServiceFees() {
  feeLoading.value = true
  try {
    const res = await getServiceFeeSummaries(feeQuery)
    feeData.value = res.data.records
    feeTotal.value = res.data.total
  } catch { /* */ } finally {
    feeLoading.value = false
  }
}

// Tab 切换时加载数据
function onTabChange(tab: string) {
  if (tab === 'onboarding') fetchOnboarding()
  else if (tab === 'packages') fetchPackages()
  else if (tab === 'deposits') fetchDeposits()
  else if (tab === 'serviceFees') fetchServiceFees()
  else if (tab === 'packageManage') fetchPackageManage()
  else if (tab === 'onboardingPlanManage') fetchOnboardingPlanManage()
}

// ========== 套餐管理 ==========
const pkgManageLoading = ref(false)
const pkgManageData = ref<PackageManageItem[]>([])
const pkgManageTotal = ref(0)
const pkgManageQuery = reactive({ page: 1, size: 20, status: null as number | null })

function packageTypeText(t: number) {
  const map: Record<number, string> = { 1: '基础版', 2: '专业版', 3: '旗舰版' }
  return map[t] || '未知'
}
function packageTypeTag(t: number): '' | 'success' | 'warning' {
  const map: Record<number, '' | 'success' | 'warning'> = { 1: '', 2: 'success', 3: 'warning' }
  return map[t] || ''
}

async function fetchPackageManage() {
  pkgManageLoading.value = true
  try {
    const res = await getPackageManage(pkgManageQuery)
    pkgManageData.value = res.data.records
    pkgManageTotal.value = res.data.total
  } catch { /* */ } finally {
    pkgManageLoading.value = false
  }
}

// 套餐状态切换
async function handleStatusChange(packageId: number, enabled: boolean) {
  try {
    await updatePackageStatus(packageId, enabled ? 1 : 0)
    ElMessage.success(enabled ? '套餐已启用' : '套餐已停用')
    fetchPackageManage()
  } catch { /* */ }
}

// 弹窗相关
const pkgDialogVisible = ref(false)
const pkgFormMode = ref<'create' | 'edit'>('create')
const pkgEditingId = ref<number | null>(null)
const pkgSubmitting = ref(false)
const pkgFormRef = ref<FormInstance>()

interface PkgFormData {
  packageName: string
  packageType: number | null
  price: number | null
  durationMonths: number | null
  serviceFeeRatePercent: number | null
  features: string
  sortOrder: number
}

const pkgForm = reactive<PkgFormData>({
  packageName: '',
  packageType: null,
  price: null,
  durationMonths: null,
  serviceFeeRatePercent: null,
  features: '',
  sortOrder: 0
})

const pkgFormRules: FormRules<PkgFormData> = {
  packageName: [{ required: true, message: '请输入套餐名称', trigger: 'blur' }],
  packageType: [{ required: true, message: '请选择套餐类型', trigger: 'change' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  durationMonths: [{ required: true, message: '请输入有效期', trigger: 'blur' }],
  serviceFeeRatePercent: [{ required: true, message: '请输入服务费率', trigger: 'blur' }]
}

function resetPkgForm() {
  pkgForm.packageName = ''
  pkgForm.packageType = null
  pkgForm.price = null
  pkgForm.durationMonths = null
  pkgForm.serviceFeeRatePercent = null
  pkgForm.features = ''
  pkgForm.sortOrder = 0
}

function showCreateDialog() {
  pkgFormMode.value = 'create'
  pkgEditingId.value = null
  resetPkgForm()
  pkgDialogVisible.value = true
}

function showEditDialog(row: PackageManageItem) {
  pkgFormMode.value = 'edit'
  pkgEditingId.value = row.packageId
  pkgForm.packageName = row.packageName
  pkgForm.packageType = row.packageType
  pkgForm.price = row.price
  pkgForm.durationMonths = row.durationMonths
  pkgForm.serviceFeeRatePercent = row.serviceFeeRate * 100
  pkgForm.features = row.features || ''
  pkgForm.sortOrder = row.sortOrder || 0
  pkgDialogVisible.value = true
}

async function handlePkgSubmit() {
  if (!pkgFormRef.value) return
  await pkgFormRef.value.validate()

  pkgSubmitting.value = true
  try {
    const payload = {
      packageName: pkgForm.packageName,
      packageType: pkgForm.packageType!,
      price: pkgForm.price!,
      durationMonths: pkgForm.durationMonths!,
      serviceFeeRate: (pkgForm.serviceFeeRatePercent || 0) / 100,
      features: pkgForm.features || undefined,
      sortOrder: pkgForm.sortOrder
    }

    if (pkgFormMode.value === 'create') {
      await createPackage(payload)
      ElMessage.success('套餐创建成功')
    } else {
      await updatePackage(pkgEditingId.value!, payload)
      ElMessage.success('套餐更新成功')
    }

    pkgDialogVisible.value = false
    fetchPackageManage()
  } catch { /* */ } finally {
    pkgSubmitting.value = false
  }
}

// ========== 入驻费套餐管理 ==========
const planManageLoading = ref(false)
const planManageData = ref<OnboardingPlanManageItem[]>([])
const planManageTotal = ref(0)
const planManageQuery = reactive({ page: 1, size: 20, status: null as number | null })

async function fetchOnboardingPlanManage() {
  planManageLoading.value = true
  try {
    const res = await getOnboardingPlanManage(planManageQuery)
    planManageData.value = res.data.records
    planManageTotal.value = res.data.total
  } catch { /* */ } finally {
    planManageLoading.value = false
  }
}

async function handlePlanStatusChange(planId: number, enabled: boolean) {
  try {
    await updateOnboardingPlanStatus(planId, enabled ? 1 : 0)
    ElMessage.success(enabled ? '套餐已启用' : '套餐已停用')
    fetchOnboardingPlanManage()
  } catch { /* */ }
}

const planDialogVisible = ref(false)
const planFormMode = ref<'create' | 'edit'>('create')
const planEditingId = ref<number | null>(null)
const planSubmitting = ref(false)
const planFormRef = ref<FormInstance>()

interface PlanFormData {
  planName: string
  planType: number | null
  feeAmount: number | null
  durationMonths: number | null
  description: string
}

const planForm = reactive<PlanFormData>({
  planName: '',
  planType: null,
  feeAmount: null,
  durationMonths: null,
  description: ''
})

const planFormRules: FormRules<PlanFormData> = {
  planName: [{ required: true, message: '请输入套餐名称', trigger: 'blur' }],
  planType: [{ required: true, message: '请选择套餐类型', trigger: 'change' }],
  feeAmount: [{ required: true, message: '请输入费用金额', trigger: 'blur' }],
  durationMonths: [{ required: true, message: '请输入有效期', trigger: 'blur' }]
}

function resetPlanForm() {
  planForm.planName = ''
  planForm.planType = null
  planForm.feeAmount = null
  planForm.durationMonths = null
  planForm.description = ''
}

function showPlanCreateDialog() {
  planFormMode.value = 'create'
  planEditingId.value = null
  resetPlanForm()
  planDialogVisible.value = true
}

function showPlanEditDialog(row: OnboardingPlanManageItem) {
  planFormMode.value = 'edit'
  planEditingId.value = row.planId
  planForm.planName = row.planName
  planForm.planType = row.planType
  planForm.feeAmount = row.feeAmount
  planForm.durationMonths = row.durationMonths
  planForm.description = row.description || ''
  planDialogVisible.value = true
}

async function handlePlanSubmit() {
  if (!planFormRef.value) return
  await planFormRef.value.validate()

  planSubmitting.value = true
  try {
    const payload = {
      planName: planForm.planName,
      planType: planForm.planType!,
      feeAmount: planForm.feeAmount!,
      durationMonths: planForm.durationMonths!,
      description: planForm.description || undefined
    }

    if (planFormMode.value === 'create') {
      await createOnboardingPlan(payload)
      ElMessage.success('入驻费套餐创建成功')
    } else {
      await updateOnboardingPlan(planEditingId.value!, payload)
      ElMessage.success('入驻费套餐更新成功')
    }

    planDialogVisible.value = false
    fetchOnboardingPlanManage()
  } catch { /* */ } finally {
    planSubmitting.value = false
  }
}

// ========== 保证金退款审核 ==========
const refundDialogVisible = ref(false)
const refundingDeposit = ref<DepositItem | null>(null)
const refundAction = ref<'approve' | 'reject'>('approve')
const refundReason = ref('')
const refundSubmitting = ref(false)

function showRefundDialog(row: DepositItem) {
  refundingDeposit.value = row
  refundAction.value = 'approve'
  refundReason.value = ''
  refundDialogVisible.value = true
}

async function handleRefundSubmit() {
  if (refundAction.value === 'reject' && !refundReason.value.trim()) {
    ElMessage.warning('请输入拒绝原因')
    return
  }

  refundSubmitting.value = true
  try {
    await handleDepositRefund(
      refundingDeposit.value!.depositId,
      refundAction.value,
      refundAction.value === 'reject' ? refundReason.value : undefined
    )
    ElMessage.success(refundAction.value === 'approve' ? '退款审核通过' : '退款已拒绝')
    refundDialogVisible.value = false
    fetchDeposits()
  } catch { /* */ } finally {
    refundSubmitting.value = false
  }
}

// ========== 服务费手动调整 ==========
const feeAdjustDialogVisible = ref(false)
const adjustingFee = ref<ServiceFeeSummaryItem | null>(null)
const feeNewStatus = ref<number>(0)
const feeAdjustReason = ref('')
const feeAdjustSubmitting = ref(false)

async function handleManualDeduct(row: ServiceFeeSummaryItem) {
  try {
    await ElMessageBox.confirm(
      `确定要手动扣减商户"${row.merchantName}"${row.yearMonth}的服务费 ¥${row.serviceFee?.toFixed(2)} 吗？`,
      '手动扣减确认',
      { type: 'warning' }
    )
    await manualDeductServiceFee(row.summaryId)
    ElMessage.success('服务费扣减成功')
    fetchServiceFees()
  } catch { /* */ }
}

function showFeeAdjustDialog(row: ServiceFeeSummaryItem) {
  adjustingFee.value = row
  feeNewStatus.value = row.deductStatus
  feeAdjustReason.value = ''
  feeAdjustDialogVisible.value = true
}

async function handleFeeAdjustSubmit() {
  if (!feeAdjustReason.value.trim()) {
    ElMessage.warning('请输入调整原因')
    return
  }

  feeAdjustSubmitting.value = true
  try {
    await adjustServiceFeeStatus(adjustingFee.value!.summaryId, feeNewStatus.value, feeAdjustReason.value)
    ElMessage.success('状态调整成功')
    feeAdjustDialogVisible.value = false
    fetchServiceFees()
  } catch { /* */ } finally {
    feeAdjustSubmitting.value = false
  }
}

onMounted(fetchOnboarding)
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.header-actions { display: flex; gap: 10px; }
.pagination { margin-top: 16px; justify-content: flex-end; }
</style>
