import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import ElementPlus from'element-plus'
import 'element-plus/dist/index.css'
import { zhCn } from 'element-plus/es/locales.mjs'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import Vue3TouchEvents from 'vue3-touch-events'
import vue3videoPlay from "vue3-video-play";
import "vue3-video-play/dist/style.css";
const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(vue3videoPlay)
app.use(ElementPlus,{locale:zhCn})
app.use(Vue3TouchEvents)
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}
app.mount('#app')
