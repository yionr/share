/*
 Navicat Premium Data Transfer

 Source Server         : ecs
 Source Server Type    : MySQL
 Source Server Version : 50733
 Source Host           : yionr.cn:3306
 Source Schema         : share

 Target Server Type    : MySQL
 Target Server Version : 50733
 File Encoding         : 65001

 Date: 02/03/2021 19:14:06
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for sfile
-- ----------------------------
DROP TABLE IF EXISTS `sfile`;
CREATE TABLE `sfile`  (
                          `fid` varchar(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
                          `name` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
                          `password` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
                          `times` int(11) NOT NULL DEFAULT 1,
                          `filetype` varchar(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'file',
                          `uid` int(11) NULL DEFAULT NULL,
                          PRIMARY KEY (`fid`) USING BTREE,
                          UNIQUE INDEX `sFile_fid_uindex`(`fid`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
                         `uid` int(11) NOT NULL AUTO_INCREMENT,
                         `email` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
                         `password` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
                         `created_time` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
                         `active` tinyint(1) NULL DEFAULT 0,
                         PRIMARY KEY (`uid`) USING BTREE,
                         UNIQUE INDEX `user_email_uindex`(`email`) USING BTREE,
                         UNIQUE INDEX `user_uid_uindex`(`uid`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 32 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

SET FOREIGN_KEY_CHECKS = 1;
