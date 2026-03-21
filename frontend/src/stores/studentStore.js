import {defineStore} from 'pinia'
import {ref} from 'vue'
import axios from 'axios'

export const useStudentStore = defineStore('student', () => {
    const students = ref([])
    const loading = ref(false)
    const lastFetchTime = ref(0)
    const CACHE_DURATION = 60000 // 1 minute cache

    async function fetchStudents(forceRefresh = false) {
        const now = Date.now()
        if (!forceRefresh && students.value.length > 0 && (now - lastFetchTime.value < CACHE_DURATION)) {
            return students.value
        }

        loading.value = true
        try {
            const res = await axios.get('/api/student/list', {
                params: { pageNum: 1, pageSize: 500 }
            })
            students.value = res.data.data.records || []
            lastFetchTime.value = now
        } catch (e) {
            console.error('Failed to fetch students:', e)
        } finally {
            loading.value = false
        }
        return students.value
    }

    function invalidateCache() {
        lastFetchTime.value = 0
    }

    return { students, loading, fetchStudents, invalidateCache }
})
