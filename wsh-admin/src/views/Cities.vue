<template>
  <div class="cities-page">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>城市管理</span>
          <div class="header-actions">
            <el-input v-model="query.keyword" placeholder="搜索城市名/编码/省份" clearable style="width: 240px" @clear="fetchData" @keyup.enter="fetchData" />
            <el-select v-model="query.status" placeholder="状态筛选" clearable style="width: 120px" @change="fetchData">
              <el-option label="已开通" :value="1" />
              <el-option label="未开通" :value="0" />
            </el-select>
            <el-button type="primary" @click="fetchData">搜索</el-button>
            <el-button type="success" @click="handleAdd">新增城市</el-button>
          </div>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="cityCode" label="城市编码" width="100" />
        <el-table-column prop="cityName" label="城市名称" width="100" />
        <el-table-column prop="provinceName" label="所属省份" width="100" />
        <el-table-column prop="pinyin" label="拼音" width="120" />
        <el-table-column label="城市等级" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="levelTag(row.level).type" size="small">{{ levelTag(row.level).text }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="merchantCount" label="商户数" width="80" align="center" />
        <el-table-column prop="activityCount" label="活动数" width="80" align="center" />
        <el-table-column prop="sortOrder" label="排序" width="70" align="center" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="openDate" label="开通日期" width="110" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        class="pagination"
        v-model:current-page="query.page"
        v-model:page-size="query.size"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next"
        @size-change="fetchData"
        @current-change="fetchData"
      />
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="editingCity ? '编辑城市' : '新增城市'" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="城市编码" prop="cityCode">
          <el-input v-model="form.cityCode" placeholder="如：440300" :disabled="!!editingCity" />
        </el-form-item>
        <el-form-item label="城市名称" prop="cityName">
          <el-input v-model="form.cityName" placeholder="如：深圳" />
        </el-form-item>
        <el-form-item label="所属省份" prop="provinceName">
          <el-input v-model="form.provinceName" placeholder="如：广东省" />
        </el-form-item>
        <el-form-item label="拼音" prop="pinyin">
          <el-input v-model="form.pinyin" placeholder="如：shenzhen" />
        </el-form-item>
        <el-form-item label="城市等级" prop="level">
          <el-select v-model="form.level" placeholder="请选择等级" style="width: 100%">
            <el-option label="一线城市" :value="1" />
            <el-option label="新一线城市" :value="2" />
            <el-option label="二线城市" :value="3" />
            <el-option label="三线及以下" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="经度" prop="longitude">
          <el-input-number v-model="form.longitude" :precision="6" :controls="false" style="width: 100%" placeholder="如：114.057865" />
        </el-form-item>
        <el-form-item label="纬度" prop="latitude">
          <el-input-number v-model="form.latitude" :precision="6" :controls="false" style="width: 100%" placeholder="如：22.543099" />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :max="999" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态" prop="status" v-if="!editingCity">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">已开通</el-radio>
            <el-radio :label="0">未开通</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { getCities, createCity, updateCity, updateCityStatus, type CityItem } from '@/api/admin'

const loading = ref(false)
const tableData = ref<CityItem[]>([])
const total = ref(0)
const query = reactive({ page: 1, size: 20, keyword: '', status: null as number | null })

const dialogVisible = ref(false)
const editingCity = ref<CityItem | null>(null)
const formRef = ref<FormInstance>()
const submitting = ref(false)

const form = reactive({
  cityCode: '',
  cityName: '',
  provinceName: '',
  pinyin: '',
  level: 3,
  longitude: undefined as number | undefined,
  latitude: undefined as number | undefined,
  sortOrder: 0,
  status: 1
})

const rules: FormRules = {
  cityCode: [{ required: true, message: '请输入城市编码', trigger: 'blur' }],
  cityName: [{ required: true, message: '请输入城市名称', trigger: 'blur' }],
  level: [{ required: true, message: '请选择城市等级', trigger: 'change' }]
}

function levelTag(level: number) {
  const map: Record<number, { type: 'danger' | 'warning' | '' | 'info'; text: string }> = {
    1: { type: 'danger', text: '一线' },
    2: { type: 'warning', text: '新一线' },
    3: { type: '', text: '二线' },
    4: { type: 'info', text: '三线及以下' }
  }
  return map[level] || { type: 'info', text: '未知' }
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getCities(query)
    tableData.value = res.data.records
    total.value = res.data.total
  } catch { /* */ } finally {
    loading.value = false
  }
}

function handleAdd() {
  editingCity.value = null
  Object.assign(form, {
    cityCode: '',
    cityName: '',
    provinceName: '',
    pinyin: '',
    level: 3,
    longitude: undefined,
    latitude: undefined,
    sortOrder: 0,
    status: 1
  })
  dialogVisible.value = true
}

function handleEdit(row: CityItem) {
  editingCity.value = row
  Object.assign(form, {
    cityCode: row.cityCode,
    cityName: row.cityName,
    provinceName: row.provinceName,
    pinyin: row.pinyin,
    level: row.level,
    longitude: row.longitude,
    latitude: row.latitude,
    sortOrder: row.sortOrder,
    status: row.status
  })
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate()
  
  submitting.value = true
  try {
    if (editingCity.value) {
      await updateCity(editingCity.value.cityId, {
        cityName: form.cityName,
        provinceName: form.provinceName,
        pinyin: form.pinyin,
        level: form.level,
        longitude: form.longitude,
        latitude: form.latitude,
        sortOrder: form.sortOrder
      })
      ElMessage.success('更新成功')
    } else {
      await createCity({
        cityCode: form.cityCode,
        cityName: form.cityName,
        provinceName: form.provinceName,
        pinyin: form.pinyin,
        level: form.level,
        longitude: form.longitude,
        latitude: form.latitude,
        sortOrder: form.sortOrder,
        status: form.status
      })
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch { /* */ } finally {
    submitting.value = false
  }
}

async function handleStatusChange(row: CityItem) {
  try {
    await updateCityStatus(row.cityId, row.status)
    ElMessage.success(row.status === 1 ? '已开通' : '已关闭')
  } catch {
    // 失败时恢复原状态
    row.status = row.status === 1 ? 0 : 1
  }
}

onMounted(fetchData)
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.header-actions {
  display: flex;
  gap: 10px;
}
.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
