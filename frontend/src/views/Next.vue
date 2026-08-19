<template>
        <div class="box">
<h1 class="title">练习区域</h1>
<div class="content-one">
    <div class="content-top">
<div style="color: black;font-size: 20px;font-weight: 500; border-left: 4px solid #00ffff;" >视频播放器</div>
<button style="color: black;">更多</button>
</div>
<div class="content">
<vue3VideoPlay v-bind="options"  />
<div class="card">
<div style="color: #000; margin-left: 15px;font-size: 20px;">
选集
</div>
<div class="setcard">
<div class="card-item"  @click="onitemClick(item)" v-for="(item, index) in 20" :key="index">
第{{ item }}集
</div>
</div>
    </div>

</div>
</div>
<h1>
    区域二
</h1>
<div class="content-one">
    <div class="content-top">
        <div style="color: black;font-size: 20px;font-weight: 500; border-left: 4px solid #00ffff;" >
            视频网站头部内容</div>
<button style="color: black;">更多</button>
    </div>
<div class="content-a">
<div class="a-left"> 
    <div class="header-a" style=" font-size: 24px;" v-for="(item, index) in 4"
     :key="index">内容{{ item }}</div>
     <div style="font-size: 24px; cursor: pointer;"  @mouseenter="isShow=true"
      @mouseleave="isShow=false">更多
     <ElIcon v-if="isShow">
        <ArrowUp/>
     </ElIcon>
     <ElIcon v-else>
        <ArrowDown/>
     </ElIcon>
     <div class="panel" v-show="isShow">
        <button class="el-btn" @click="goRank">排行榜</button>
        <button class="el-btn" @click="goCommunity">留言板</button>
        <button class="el-btn" @click="goProfile">我的追番</button>
        <div class="el-btn" @click="goTonext">隐藏区域</div>
      </div>
     </div>
</div>
<div class="a-right">
     <div class="search-box">
         <input type="text" placeholder="你好">
          <ElIcon class="icon">
        <Search/>
    </ElIcon> 
        </div> 
   <!--
    -->
</div>
</div>
</div>
<h1>
    区域三 
</h1>
<div class="content-one">
    <div class="content-top">
        <div style="color: black;font-size: 20px;font-weight: 500; border-left: 4px solid #00ffff;" >
            视频播放器</div>
            
<button style="color: black;">更多</button>
</div>
<el-dropdown trigger="hover" placement="bottom-end">
    <span class="more-trigger">
      更多AAAAAAAAA
      <el-icon><ArrowDown /></el-icon>
    </span>
    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item>动作片</el-dropdown-item>
        <el-dropdown-item>科幻片</el-dropdown-item>
        <el-dropdown-item divided>设置</el-dropdown-item>
        <el-dropdown-item>退出登录</el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</div>

        </div>
</template>
    
<script setup>
import { ArrowDown, ArrowUp, Search } from "@element-plus/icons-vue";
import { ElButton, ElIcon, ElMention, ElMessage } from "element-plus";
import { reactive, ref } from "vue";
const isShow=ref(false)

const options = reactive({
width: "700px",
height: "360px",
color: "#409eff",
title: "视频标题",
src: "https://video.699pic.com/videos/53/35/69/a_66f812011e32917275335691254_10s.mp4",
muted: false,
webFullScreen: false,
speedRate: ["0.75", "1.0", "1.25", "1.5", "2.0"],
autoPlay: false,
loop: false,
mirror: false,
ligthOff: false,
volume: 0.3,
control: true,
controlBtns: [
"audioTrack",
"quality",
"speedRate",
"volume",
"setting",
"pip",
"pageFullScreen",
"fullScreen",
],
});
const keyword = ref('')

function onSearch() {
  if (!keyword.value.trim()) return
  // 执行搜索逻辑
  console.log('搜索:', keyword.value)
}
    function  onitemClick(item){
        if(item==1){
            ElMessage.success('当前已经在第一集了')
        }else{
              ElMessage.primary('暂未支持切换到第'+item+'集')
        }
    
    }
</script>
    
<style scoped>
.panel{
      width: 380px;
  min-height: 60px;
  position: absolute;
  top: 50%;
  margin-top: 30px;
  background: rgba(10, 12, 18, 0.95);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 10px;
  left: 300px;
  z-index: 999;
  display: flex;
  flex-direction: row;
  align-items: center;
  padding: 10px 14px;
  gap: 6px;
}
.more-trigger {
  font-size: 24px;
  color: #fff;
  background-color: chocolate;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}
.content-a{
    width: 100%;
    height: 50px;
background-color: #000;
margin-top: 10px;
box-sizing: border-box;
padding: 0 30px;
display: flex;
justify-content: space-between;
}
.a-left{
    width: 50%;
    height: 100%;
    /* background-color: #00ffff; */
    display: flex;
    flex-direction: row;
    align-items: center;
    gap: 36px;
    box-sizing: border-box;
    padding-left: 20px;
    position: relative;
}
.a-right{
    width: 40%;
    height: 100%;
    /* background-color: #00ffff;   */
    display: flex;
    flex-direction: row;
    box-sizing: border-box;
    padding-left: 10%;
    align-items: center;
}

.search-box{
    height: 30px;
    border-radius: 5px;
    width: 150px;
    background-color: #fff;
    border: none;
    padding: 0 5px;
    box-sizing: border-box;
    position: relative;
  display: flex;
  align-items: center;
  flex-direction: row;
}
.search-box input{
    width: 80%;
    height: 100%;
    padding: 0;
    border: none;
outline: none;
}

.icon{
  position: absolute;
  color: #000;
  right: 10px;
  cursor: pointer;
}
    .box{
        width: 100%;
        height: 500vh;
        display: flex;
        flex-direction: column;
        align-items: center;

    }
    .content-one{
        width: 90%;
        height: 400px;
        background-color: aliceblue;
        display: flex;
        flex-direction: column;
        
    }
    .content-top{
 width: 100%;
 height: 50px;
 background-color: #00ffff;
 display: flex;
 flex-direction: row;
 justify-content: space-between;
 padding: 0 12px;
 box-sizing: border-box;
 align-items: center;
    }
    .header-a:hover{
color: #00ffff;
cursor: pointer;
    }
    button{
        width: 60px;
        height: 30px;
        font-size: 20px;
        background-color: rgba(0, 0, 0, 0);
        border: none;
        color: #fff;
        cursor: pointer;
    }
    button:hover {
  color: #00ffff;
  border-color: #00ffff;
}
.content{
  width: 100%;
  height: 100%;
    display: flex;
    flex-direction: row;
}
.card{
flex: 1;
display: flex;
flex-direction: column;
}
.setcard{
    flex: 1;
    display: grid;
    grid-template-columns: 1fr 1fr 1fr 1fr 1fr;
    padding: 10px;
    gap: 20px;
    box-sizing: border-box;
}
.card-item{
    height: 40px;
    display: flex;
    justify-content: center;
    align-items: center;
    color: #000;
    cursor: pointer;
    border-radius: 6px;
    background-color: aquamarine;
}
</style>