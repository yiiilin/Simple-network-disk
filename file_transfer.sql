/*
Navicat MySQL Data Transfer

Source Server         : localhost
Source Server Version : 80017
Source Host           : localhost:3306
Source Database       : file_transfer

Target Server Type    : MYSQL
Target Server Version : 80017
File Encoding         : 65001

Date: 2020-06-04 18:19:01
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for `structure`
-- ----------------------------
DROP TABLE IF EXISTS `structure`;
CREATE TABLE `structure` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `uid` int(11) DEFAULT NULL,
  `path` varchar(100) DEFAULT NULL,
  `uuid` varchar(32) DEFAULT NULL,
  `name` varchar(50) DEFAULT NULL,
  `size` bigint(20) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `type` varchar(10) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=78 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of structure
-- ----------------------------
INSERT INTO `structure` VALUES ('74', '1', '/', '9db4dcc1dfb949198dda5efe7ea16f6f', '123', '-1', '2020-06-03 22:46:36', 'dir');
INSERT INTO `structure` VALUES ('75', '1', '/123', 'cd3d6c7c27924eb8a03579b9710a8a41', 'ideaIU-2019.3.3.exe', '706085896', '2020-06-03 22:46:40', 'file');
INSERT INTO `structure` VALUES ('76', '1', '/123', '9e3ed2feb6934553a39be263b77c549c', '红色警戒2终极版.exe', '1550831775', '2020-06-03 22:52:04', 'file');
INSERT INTO `structure` VALUES ('77', '1', '/123', '888366f1ff4741189d879da403694cbc', 'bf2_2019.exe', '3803073098', '2020-06-03 22:56:16', 'file');

-- ----------------------------
-- Table structure for `user`
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `uid` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) DEFAULT NULL,
  `password` varchar(60) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL,
  PRIMARY KEY (`uid`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES ('1', 'qwe', '$2a$10$Y3tZXO1PsHQm44JH/z4w3uPZDKdM0j5ib8vNBSQDcaa0.t64k//n6');
