-- ============================================================
-- 动漫网站功能扩展 —— 数据库初始化脚本
-- 用法：mysql -uroot -p123456 < init_anime.sql
-- 可重复执行（DROP 后重建动漫相关表，不动 t_user）
-- ============================================================

USE exam_auth;

-- ---------- 番剧表 ----------
DROP TABLE IF EXISTS t_comment;
DROP TABLE IF EXISTS t_favorite;
DROP TABLE IF EXISTS t_history;
DROP TABLE IF EXISTS t_danmaku;
DROP TABLE IF EXISTS t_anime;

CREATE TABLE t_anime (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    title       VARCHAR(100) NOT NULL COMMENT '标题',
    author      VARCHAR(100) NOT NULL DEFAULT '' COMMENT '制作方',
    category    VARCHAR(20)  NOT NULL COMMENT '分类：国漫/日漫/欧美动漫/电影',
    tags        VARCHAR(200) NOT NULL DEFAULT '' COMMENT '标签，逗号分隔',
    description VARCHAR(1000) NOT NULL DEFAULT '' COMMENT '简介',
    cover       VARCHAR(255) NOT NULL DEFAULT '' COMMENT '封面图',
    rating      DECIMAL(3,1) NOT NULL DEFAULT 0 COMMENT '评分',
    year        INT          NOT NULL DEFAULT 0 COMMENT '年份',
    views       BIGINT       NOT NULL DEFAULT 0 COMMENT '播放量',
    episodes    INT          NOT NULL DEFAULT 1 COMMENT '总集数',
    is_banner   TINYINT      NOT NULL DEFAULT 0 COMMENT '是否轮播推荐',
    bili_season VARCHAR(30)  NOT NULL DEFAULT '' COMMENT 'B站番剧season_id（正片源，优先）',
    bvid        VARCHAR(50)  NOT NULL DEFAULT '' COMMENT 'B站视频BV号（无season时的填充源）',
    video_url   VARCHAR(500) NOT NULL DEFAULT '' COMMENT '直链视频地址（保留备用）',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_category (category),
    KEY idx_views (views),
    KEY idx_rating (rating)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='番剧表';

-- ---------- 弹幕表 ----------
CREATE TABLE t_danmaku (
    id         BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    anime_id   BIGINT        NOT NULL COMMENT '番剧id',
    ep_no      INT           NOT NULL DEFAULT 1 COMMENT '集数',
    time       DECIMAL(10,3) NOT NULL DEFAULT 0 COMMENT '弹幕时间点（秒）',
    text       VARCHAR(200)  NOT NULL COMMENT '弹幕内容',
    color      VARCHAR(20)   NOT NULL DEFAULT '#ffffff' COMMENT '弹幕颜色',
    username   VARCHAR(50)   NOT NULL DEFAULT '游客' COMMENT '发送人',
    created_at DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_anime_ep (anime_id, ep_no)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='弹幕表';

-- ---------- 观看历史表 ----------
CREATE TABLE t_history (
    id         BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id    BIGINT        NOT NULL COMMENT '用户id',
    anime_id   BIGINT        NOT NULL COMMENT '番剧id',
    ep_no      INT           NOT NULL DEFAULT 1 COMMENT '看到第几集',
    progress   DECIMAL(10,3) NOT NULL DEFAULT 0 COMMENT '播放进度（秒）',
    updated_at DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_anime (user_id, anime_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='观看历史表';

-- ---------- 收藏表 ----------
CREATE TABLE t_favorite (
    id         BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id    BIGINT   NOT NULL COMMENT '用户id',
    anime_id   BIGINT   NOT NULL COMMENT '番剧id',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_anime (user_id, anime_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='收藏表';

-- ---------- 评论表 ----------
CREATE TABLE t_comment (
    id         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    anime_id   BIGINT       NOT NULL COMMENT '番剧id',
    user_id    BIGINT       NOT NULL COMMENT '用户id',
    username   VARCHAR(50)  NOT NULL COMMENT '用户名',
    content    VARCHAR(500) NOT NULL COMMENT '评论内容',
    likes      INT          NOT NULL DEFAULT 0 COMMENT '点赞数',
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_anime (anime_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='评论表';

-- ============================================================
-- 种子数据：16 部番剧
-- bvid 为 B站真实视频（官方嵌入线路）；video_url 为公版动画直链（兜底线路）
-- ============================================================

INSERT INTO t_anime (id, title, author, category, tags, description, cover, rating, year, views, episodes, is_banner, bili_season, bvid, video_url) VALUES
-- 国漫
(1, '刺客伍六七', '小疯映画', '国漫', '热血,搞笑,战斗',
 '小鸡岛最强发型师阿七，白天在理发店帮街坊剪头，晚上却是一位神秘杀手组织排名垫底的高级刺客。\n每次出任务总把场面搞得鸡飞狗跳，却在刀光剑影中一次次守护着这座小岛的平凡日常。\n一边吐槽一边接单，一边剪发一边暗杀，爆笑之下藏着温柔的人间烟火。',
 '/covers/lunbo1.jpg', 9.2, 2018, 360000000, 40, 1, 'ss6360', 'BV1SE411x7Z4',
 'https://upload.wikimedia.org/wikipedia/commons/e/eb/%E5%B0%8F%E8%9D%8C%E8%9A%AA%E6%89%BE%E5%A6%88%E5%A6%88_1960.webm'),
(2, '罗小黑战记', 'MTJJ', '国漫', '治愈,奇幻,萌系',
 '一只被自然孕育的小猫妖罗小黑，在森林家园被毁后踏上流浪之路，意外被人类少女罗小白收留。\n它一边学习融入人类世界，一边面对来自妖界与人类的种种挑战，逐渐明白"家"的真正含义。\n画风清新治愈，节奏舒缓温柔，每一帧都是对疲惫心灵的抚慰。',
 '/covers/lunbo2.jpg', 9.5, 2011, 210000000, 28, 1, 'ss1733', 'BV1Qx411B7rP',
 'https://upload.wikimedia.org/wikipedia/commons/e/eb/%E5%B0%8F%E8%9D%8C%E8%9A%AA%E6%89%BE%E5%A6%88%E5%A6%88_1960.webm'),
(3, '灵笼', '艺画开天', '国漫', '科幻,末世,冒险',
 '末日浩劫之后，人类蜷缩在一座悬浮空中的"灯塔"上，依靠严苛的等级制度苟延残喘。\n猎荒者小队一次次冒险前往地面搜集物资，与恐怖的噬极兽和未知的生态对抗。\n当灯塔的秘密被层层揭开，幸存者们不得不直面"人类该以何种姿态活下去"的终极拷问。',
 '/covers/lunbo3.jpg', 8.9, 2019, 520000000, 16, 1, '', 'BV1LP4y1H7TJ',
 'https://upload.wikimedia.org/wikipedia/commons/transcoded/c/c0/Big_Buck_Bunny_4K.webm/Big_Buck_Bunny_4K.webm.720p.vp9.webm'),
(4, '时光代理人', '澜映画', '国漫', '悬疑,奇幻,催泪',
 '程小时能进入照片定格的那一瞬，陆光则能预知未来十二小时的走向，两人配合替委托人弥补遗憾。\n可每一次"回到过去"都牵动着蝴蝶效应，他们逐渐发现自己卷入一场无法改变的命运漩涡。\n悬疑烧脑与细腻情感交织，被无数观众称为"国漫之光"。',
 '/covers/lunbo4.jpg', 9.1, 2021, 470000000, 24, 1, '', 'BV1dD4y1K7zP',
 'https://upload.wikimedia.org/wikipedia/commons/c/cb/Tears_of_Steel_1080p.webm'),
-- 日漫
(5, '鬼灭之刃', 'ufotable', '日漫', '热血,战斗,神作',
 '大正时代，少年炭治郎一家惨遭鬼王灭门，唯一幸存的妹妹祢豆子也变成了鬼。\n为了寻找让妹妹变回人类的方法，他加入鬼杀队，以"水之呼吸"斩鬼前行，一路结识伙伴、直面宿敌。\nufotable 的顶级制作让每一帧打斗都燃到极致，温柔与热血并存。',
 '/covers/new1.avif', 9.4, 2019, 1280000000, 55, 1, 'ss26801', 'BV1axaVzREWu',
 'https://upload.wikimedia.org/wikipedia/commons/0/02/Elephants_Dream%28HQ%29.webm'),
(6, '咒术回战', 'MAPPA', '日漫', '热血,战斗,校园',
 '高中生虎杖悠仁为救人吞下特级咒物"两面宿傩的手指"，从此卷入咒术师与诅咒的残酷世界。\n他进入东京咒术高专，与伏黑惠、钉崎野蔷薇并肩作战，在"正确死亡"的信条下直面绝望。\n节奏凌厉、打斗华丽，黑暗世界观下的少年成长故事。',
 '/covers/new2.avif', 9.0, 2020, 890000000, 47, 1, 'ss34430', 'BV1PjagzNEYk',
 'https://upload.wikimedia.org/wikipedia/commons/c/cb/Tears_of_Steel_1080p.webm'),
(7, '间谍过家家', 'WIT STUDIO', '日漫', '喜剧,家庭,治愈',
 '间谍"黄昏"为完成任务组建临时家庭：妻子是职业杀手，女儿竟是能读心的超能力少女。\n三个人各怀秘密，却意外地成为彼此真正的家人，一边瞒天过海一边上演温馨爆笑日常。\n阿尼亚的"哇库哇库"火遍全网，全家福式快乐治愈力满分。',
 '/covers/new3.avif', 9.3, 2022, 650000000, 37, 1, 'ss41410', 'BV1sSQkYcENu',
 'https://upload.wikimedia.org/wikipedia/commons/transcoded/c/c0/Big_Buck_Bunny_4K.webm/Big_Buck_Bunny_4K.webm.720p.vp9.webm'),
(8, '进击的巨人', 'MAPPA', '日漫', '奇幻,热血,史诗',
 '高墙之内的人类安居百年，直到超大型巨人破墙而入，少年艾伦眼睁睁看着母亲被吞食。\n他发誓驱逐所有巨人，加入调查兵团，却在一次次战斗与真相中陷入自由与绝望的漩涡。\n宏大的世界观与残酷的战争叙事，被奉为平成年代最伟大的动漫之一。',
 '/covers/new4.avif', 9.6, 2013, 1520000000, 87, 1, '', 'BV1Qx411B7rP',
 'https://upload.wikimedia.org/wikipedia/commons/0/02/Elephants_Dream%28HQ%29.webm'),
-- 欧美动漫
(9, '瑞克和莫蒂', 'Adult Swim', '欧美动漫', '科幻,脑洞,黑色幽默',
 '天才科学家外公瑞克带着外孙莫蒂穿越无数平行宇宙，从虫洞跳跃到意识交换，脑洞开到天际。\n荒诞、致郁、讽刺、深情并存，每一集都是对流行文化和存在主义的疯狂解构。\n"哇，莫蒂，这太有意思了"——成年人专属的科幻喜剧神作。',
 '/covers/lunbo1.jpg', 9.7, 2013, 940000000, 71, 1, '', 'BV1dD4y1K7zP',
 'https://upload.wikimedia.org/wikipedia/commons/transcoded/c/c0/Big_Buck_Bunny_4K.webm/Big_Buck_Bunny_4K.webm.720p.vp9.webm'),
(10, '降世神通', 'Nickelodeon', '欧美动漫', '奇幻,冒险,经典',
 '四国对应四种元素，世界由"神通"维持平衡。百年冰封的少年神通王安昂醒来，肩负终结百年战乱的重任。\n他与伙伴们踏遍四国，学习御术、结识盟友，在欢笑与成长中一点点走向宿命。\n东方哲学与美式叙事融合的经典之作，全年龄向冒险史诗。',
 '/covers/lunbo2.jpg', 9.3, 2005, 780000000, 61, 1, 'ss41202', 'BV1HNEi6nEe5',
 'https://upload.wikimedia.org/wikipedia/commons/c/cb/Tears_of_Steel_1080p.webm'),
(11, '怪诞小镇', 'Disney', '欧美动漫', '悬疑,搞笑,温馨',
 '双胞胎兄妹迪普和梅宝被送到神秘小镇"重力泉"度过暑假，投奔古怪的斯坦叔公经营的神秘小屋。\n小镇的每个角落都藏着超自然怪谈：会说话的猪、隐藏在日记里的巨大秘密……\n悬疑与搞笑完美平衡，结局的伏笔回收让无数观众为之落泪。',
 '/covers/lunbo3.jpg', 9.2, 2012, 560000000, 40, 1, '', 'BV1LP4y1H7TJ',
 'https://upload.wikimedia.org/wikipedia/commons/0/02/Elephants_Dream%28HQ%29.webm'),
(12, '马男波杰克', 'Netflix', '欧美动漫', '致郁,成人,讽刺',
 '过气明星马男波杰克住进好莱坞的豪宅，面对中年危机、自我厌恶与过往的烂账。\n黑色幽默背后是扎心的现实：成功过、失败过、伤害过别人也被别人伤害过。\n"想要变成更好的人"的执念贯穿全剧，致郁又真实到令人窒息。',
 '/covers/lunbo4.jpg', 9.5, 2014, 610000000, 77, 1, '', 'BV1SE411x7Z4',
 'https://upload.wikimedia.org/wikipedia/commons/e/eb/%E5%B0%8F%E8%9D%8C%E8%9A%AA%E6%89%BE%E5%A6%88%E5%A6%88_1960.webm'),
-- 电影
(13, '蜘蛛侠：平行宇宙', 'Sony Pictures', '电影', '超英,视觉,燃',
 '高中生迈尔斯被放射性蜘蛛咬伤，成为新一任蜘蛛侠，却发现平行宇宙的裂缝打开了。\n来自不同宇宙的蜘蛛侠们齐聚一堂，面对共同的敌人，也面对各自的成长。\n颠覆性的漫画视觉风格拿下奥斯卡最佳动画长片，每一帧都是艺术品。',
 '/covers/new1.avif', 9.0, 2018, 430000000, 1, 0, '', 'BV1Qx411B7rP',
 'https://upload.wikimedia.org/wikipedia/commons/transcoded/c/c0/Big_Buck_Bunny_4K.webm/Big_Buck_Bunny_4K.webm.720p.vp9.webm'),
(14, '你的名字。', '新海诚', '电影', '爱情,奇幻,催泪',
 '乡下少女三叶与东京少年泷，在梦中互换身体，素未谋面的两人被命运悄然相连。\n当彗星划过天际，时空的谜底揭开，他们拼命寻找彼此的名字。\n新海诚导演的巅峰之作，画面美到窒息，配乐与泪点齐飞。',
 '/covers/new2.avif', 9.4, 2016, 820000000, 1, 0, '', 'BV1dD4y1K7zP',
 'https://upload.wikimedia.org/wikipedia/commons/c/cb/Tears_of_Steel_1080p.webm'),
(15, '千与千寻', '宫崎骏', '电影', '经典,奇幻,治愈',
 '少女千寻随父母误入神明世界，父母因贪吃变成猪，她被迫留在汤屋打工求生。\n在油屋的日日夜夜，她学会勇敢、善良与独立，也唤醒了被夺走名字的"白龙"。\n宫崎骏最负盛名的作品，斩获奥斯卡最佳动画长片，一代人的童年记忆。',
 '/covers/new3.avif', 9.8, 2001, 1050000000, 1, 0, '', 'BV1LP4y1H7TJ',
 'https://upload.wikimedia.org/wikipedia/commons/0/02/Elephants_Dream%28HQ%29.webm'),
(16, '天气之子', '新海诚', '电影', '爱情,奇幻,唯美',
 '离家出走的少年帆高来到东京，遇见能让天空放晴的"晴女"阳菜。\n两人靠"祈祷晴天"换取生活，却在一次次放晴中付出沉重代价。\n东京的大雨与阳光交织，少年少女为爱做出与世界为敌的选择。',
 '/covers/new4.avif', 9.1, 2019, 590000000, 1, 0, '', 'BV1SE411x7Z4',
 'https://upload.wikimedia.org/wikipedia/commons/transcoded/c/c0/Big_Buck_Bunny_4K.webm/Big_Buck_Bunny_4K.webm.720p.vp9.webm');

-- 演示弹幕（第1部番剧第1集）
INSERT INTO t_danmaku (anime_id, ep_no, time, text, color, username) VALUES
(1, 1, 1.5, '前排围观！', '#ffffff', 'admin'),
(1, 1, 3.2, '经典中的经典', '#ffd86b', 'admin'),
(1, 1, 5.0, '打卡留名', '#7ee8ff', 'admin');

-- 演示评论（第1部番剧）
INSERT INTO t_comment (anime_id, user_id, username, content, likes) VALUES
(1, 1, 'admin', '阿七的理发店什么时候开分店？', 12),
(1, 1, 'admin', '每一刀都是人情世故，太好看了！', 5);
