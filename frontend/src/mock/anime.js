// 番剧本地数据（mock 用，后续可替换为后端接口返回）
import lunbo1 from '@/assets/lunbo/lunbo1.jpg'
import lunbo2 from '@/assets/lunbo/lunbo2.jpg'
import lunbo3 from '@/assets/lunbo/lunbo3.jpg'
import lunbo4 from '@/assets/lunbo/lunbo4.jpg'
import new1 from '@/assets/lunbo/new1.avif'
import new2 from '@/assets/lunbo/new2.avif'
import new3 from '@/assets/lunbo/new3.avif'
import new4 from '@/assets/lunbo/new4.avif'

// 分类定义（首页 tab 用）
export const CATEGORIES = ['全部', '国漫', '日漫', '欧美动漫', '电影']

const animeList = [
  // ---------- 国漫 ----------
  {
    id: 1,
    title: '刺客伍六七',
    author: '小疯映画',
    category: '国漫',
    tags: ['热血', '搞笑', '战斗'],
    desc: '小鸡岛最强发型师阿七，一边接单剪发一边暗中刺杀，刀光剑影下藏着温暖人间日常。',
    cover: lunbo1,
    rating: 9.2,
    year: 2018,
    views: '3.6亿',
    episodes: 40
  },
  {
    id: 2,
    title: '罗小黑战记',
    author: 'MTJJ',
    category: '国漫',
    tags: ['治愈', '奇幻', '萌系'],
    desc: '猫妖罗小黑的流浪之旅，一路遇见温暖的人与事，治愈每一颗疲惫的心。',
    cover: lunbo2,
    rating: 9.5,
    year: 2011,
    views: '2.1亿',
    episodes: 28
  },
  {
    id: 3,
    title: '灵笼',
    author: '艺画开天',
    category: '国漫',
    tags: ['科幻', '末世', '冒险'],
    desc: '末日灯塔之上，人类最后的幸存者与地面未知力量展开殊死搏斗。',
    cover: lunbo3,
    rating: 8.9,
    year: 2019,
    views: '5.2亿',
    episodes: 16
  },
  {
    id: 4,
    title: '时光代理人',
    author: '澜映画',
    category: '国漫',
    tags: ['悬疑', '奇幻', '催泪'],
    desc: '能进入照片的两人，替委托人弥补遗憾，却一次次卷入无法改变的命运。',
    cover: lunbo4,
    rating: 9.1,
    year: 2021,
    views: '4.7亿',
    episodes: 24
  },
  // ---------- 日漫 ----------
  {
    id: 5,
    title: '鬼灭之刃',
    author: 'ufotable',
    category: '日漫',
    tags: ['热血', '战斗', '神作'],
    desc: '少年炭治郎为让妹妹变回人类，踏上斩鬼之路，水之呼吸燃爆全场。',
    cover: new1,
    rating: 9.4,
    year: 2019,
    views: '12.8亿',
    episodes: 55
  },
  {
    id: 6,
    title: '咒术回战',
    author: 'MAPPA',
    category: '日漫',
    tags: ['热血', '战斗', '校园'],
    desc: '高中生虎杖悠仁吞下特级咒物，卷入咒术师与诅咒之间的死斗。',
    cover: new2,
    rating: 9.0,
    year: 2020,
    views: '8.9亿',
    episodes: 47
  },
  {
    id: 7,
    title: '间谍过家家',
    author: 'WIT STUDIO',
    category: '日漫',
    tags: ['喜剧', '家庭', '治愈'],
    desc: '间谍、杀手、超能力少女组成临时家庭，各怀秘密却温暖爆笑。',
    cover: new3,
    rating: 9.3,
    year: 2022,
    views: '6.5亿',
    episodes: 37
  },
  {
    id: 8,
    title: '进击的巨人',
    author: 'MAPPA',
    category: '日漫',
    tags: ['奇幻', '热血', '史诗'],
    desc: '高墙之内的人类向巨人发起反击，自由与绝望交织的史诗终章。',
    cover: new4,
    rating: 9.6,
    year: 2013,
    views: '15.2亿',
    episodes: 87
  },
  // ---------- 欧美动漫 ----------
  {
    id: 9,
    title: '瑞克和莫蒂',
    author: 'Adult Swim',
    category: '欧美动漫',
    tags: ['科幻', '脑洞', '黑色幽默'],
    desc: '疯狂科学家爷爷带着外孙穿越无数平行宇宙，脑洞大开荒诞到飞起。',
    cover: lunbo1,
    rating: 9.7,
    year: 2013,
    views: '9.4亿',
    episodes: 71
  },
  {
    id: 10,
    title: '降世神通',
    author: 'Nickelodeon',
    category: '欧美动漫',
    tags: ['奇幻', '冒险', '经典'],
    desc: '神通王安昂与伙伴们踏遍四国，掌控四大元素，终结百年战乱。',
    cover: lunbo2,
    rating: 9.3,
    year: 2005,
    views: '7.8亿',
    episodes: 61
  },
  {
    id: 11,
    title: '怪诞小镇',
    author: 'Disney',
    category: '欧美动漫',
    tags: ['悬疑', '搞笑', '温馨'],
    desc: '双胞胎兄妹在神秘小镇度过暑假，每个角落都藏着超自然怪谈。',
    cover: lunbo3,
    rating: 9.2,
    year: 2012,
    views: '5.6亿',
    episodes: 40
  },
  {
    id: 12,
    title: '马男波杰克',
    author: 'Netflix',
    category: '欧美动漫',
    tags: ['致郁', '成人', '讽刺'],
    desc: '过气明星马男的中年危机实录，黑色幽默背后是扎心的现实。',
    cover: lunbo4,
    rating: 9.5,
    year: 2014,
    views: '6.1亿',
    episodes: 77
  },
  // ---------- 电影 ----------
  {
    id: 13,
    title: '蜘蛛侠：平行宇宙',
    author: 'Sony Pictures',
    category: '电影',
    tags: ['超英', '视觉', '燃'],
    desc: '多个宇宙的蜘蛛侠齐聚一堂，颠覆性的视觉风格惊艳全球。',
    cover: new1,
    rating: 9.0,
    year: 2018,
    views: '4.3亿',
    episodes: 1
  },
  {
    id: 14,
    title: '你的名字。',
    author: '新海诚',
    category: '电影',
    tags: ['爱情', '奇幻', '催泪'],
    desc: '跨越时空的互换身体，彗星之下的命运相遇，新海诚巅峰之作。',
    cover: new2,
    rating: 9.4,
    year: 2016,
    views: '8.2亿',
    episodes: 1
  },
  {
    id: 15,
    title: '千与千寻',
    author: '宫崎骏',
    category: '电影',
    tags: ['经典', '奇幻', '治愈'],
    desc: '误入神明世界的少女，在汤屋打工寻回名字与回家的路。',
    cover: new3,
    rating: 9.8,
    year: 2001,
    views: '10.5亿',
    episodes: 1
  },
  {
    id: 16,
    title: '天气之子',
    author: '新海诚',
    category: '电影',
    tags: ['爱情', '奇幻', '唯美'],
    desc: '能让天空放晴的少女与离家少年，在东京的雨里做出选择。',
    cover: new4,
    rating: 9.1,
    year: 2019,
    views: '5.9亿',
    episodes: 1
  }
]

// 可播放视频源（公开测试视频源；播放页支持换源轮换）
export const VIDEO_SOURCES = [
  'https://media.w3.org/2010/05/sintel/trailer.mp4',
  'https://media.w3.org/2010/05/bunny/trailer.mp4',
  'https://media.w3.org/2010/05/video/movie_300.mp4',
  'https://interactive-examples.mdn.mozilla.net/media/cc0-videos/flower.mp4',
  'https://interactive-examples.mdn.mozilla.net/media/cc0-videos/friday.mp4',
  'https://vjs.zencdn.net/v/oceans.mp4'
]

// 每部番剧分配一个主视频源（按 id 轮换）
animeList.forEach((item, i) => {
  item.videoUrl = VIDEO_SOURCES[i % VIDEO_SOURCES.length]
})

export default animeList
