import request from './request'

export const getAttendanceList = (params) => {
  return request.get('/attendance/list', { params })
}

export const addAttendance = (data) => {
  return request.post('/attendance/add', data)
}

export const updateAttendance = (data) => {
  return request.put('/attendance/update', data)
}

export const deleteAttendance = (id) => {
  return request.delete(`/attendance/delete/${id}`)
}

export const getStudentAttendance = (studentId) => {
  return request.get(`/attendance/student/${studentId}`)
}

