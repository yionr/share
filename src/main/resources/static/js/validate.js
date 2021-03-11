/**
 * 初始化表单验证
 */
$.validator.addMethod("checkPassword", function (value, element) {
    let checkPwd = /^[_0-9a-zA-z]{8,16}$/;
    return this.optional(element) || (checkPwd.test(value));
}, "密码只能包含数字、字母、下划线，长度为8~16位");
$.validator.addMethod("checkCodePassword", function (value, element) {
    let checkPwd = /^[_0-9a-zA-z]{0,16}$/;
    return this.optional(element) || (checkPwd.test(value));
}, "密码只能包含数字、字母、下划线，最长长度为16位");
$(function () {
    $('#loginModal form').validate({
        onkeyup: false,
        rules: {
            email: {
                required: true,
                email: true,
                remote: {
                    url: 'checkLogEmail',
                    type: 'post'
                }
            },
            password: {
                required: true,
                checkPassword: true
            }
        },
        messages: {
            email: {
                remote: '该账号不存在或未激活'
            }
        }
    });
    $('#regeditModal form').validate({
        onkeyup: false,
        rules: {
            email: {
                required: true,
                email: true,
                remote: {
                    url: 'checkRegEmail',
                    type: 'post'
                }
            },
            password: {
                required: true,
                checkPassword: true
            },
            confirmPassword: {
                required: true,
                checkPassword: true,
                equalTo: '#regPassword'
            }
        },
        messages: {
            email: {
                remote: '该邮箱已被注册，请重新注册'
            },
            confirmPassword: {
                equalTo: function () {
                    rollback(confirmRegPassword,regPassword,'两次输入的密码不一致')
                    $('#regEmail').removeAttr('readOnly')
                    return null
                }
            }
        }
    });
    $('#changePasswordModal form').validate({
        onkeyup: false,
        rules: {
            oldPassword: {
                required: true,
                checkPassword: true,
                remote: {
                    url: 'checkPassword',
                    type: 'post',
                    data: {
                        oldPassword: function () {
                            return CryptoJS.MD5($('#oldPassword').val()).toString()
                        }
                    }
                }
            },
            newPassword: {
                required: true,
                checkPassword: true
            },
            confirmPassword: {
                required: true,
                checkPassword: true,
                equalTo: '#newPassword'
            }
        },
        messages: {
            oldPassword: {
                remote: function () {
                    return '密码不正确'
                }
            },
            confirmPassword: {
                equalTo: function () {
                    rollback(confirmNewPassword,newPassword,'两次输入的密码不一致')
                    return null
                }
            }
        }
    });
    $('#sendModal form').validate({
        rules: {
            times: {
                required: true,
                number: true,
            },
            password: {
                checkCodePassword: true
            }
        },
        messages: {
            times: {
                max: '超出允许下载的范围，游客只允许范围为1-9，注册用户为1-99'
            }
        }
    });
    $('#receiveModal form').validate({
        rules: {
            fid: {
                required: true,
                number: true
            },
            password: {
                checkCodePassword: true
            }
        }
    });
})