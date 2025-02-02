<script setup>
import { ref, provide, watch } from 'vue'
import { RouterView } from 'vue-router'

const username = ref(localStorage.getItem('username') || '未登录')

// 提供更新用户名的方法
const updateUsername = (newUsername) => {
  username.value = newUsername
}

provide('updateUsername', updateUsername)
// 同时提供 username ref，这样子组件可以直接使用
provide('username', username)

// 监听 username 的变化
watch(username, (newValue) => {
  localStorage.setItem('username', newValue)
})
</script>

<template>
  <div class="app-container">
    <RouterView />
  </div>
</template>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: Arial, sans-serif;
}
</style>