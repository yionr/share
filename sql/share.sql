/*
 Navicat Premium Data Transfer

 Source Server         : ecs
 Source Server Type    : MySQL
 Source Server Version : 50732
 Source Host           : yionr.cn:3306
 Source Schema         : share

 Target Server Type    : MySQL
 Target Server Version : 50732
 File Encoding         : 65001

 Date: 02/12/2020 09:59:32
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for sfile
-- ----------------------------
DROP TABLE IF EXISTS `sfile`;
CREATE TABLE `sfile`  (
  `fid` varchar(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `times` int(11) NOT NULL DEFAULT 1,
  `uid` int(11) NULL DEFAULT NULL,
  PRIMARY KEY (`fid`) USING BTREE,
  UNIQUE INDEX `sFile_fid_uindex`(`fid`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sfile
-- ----------------------------

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `uid` int(11) NOT NULL,
  `email` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_time` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `active` boolean default false,
  PRIMARY KEY (`uid`) USING BTREE,
  UNIQUE INDEX `user_email_uindex`(`email`) USING BTREE,
  UNIQUE INDEX `user_uid_uindex`(`uid`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
