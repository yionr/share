/**
 * 初始化表单验证
 */
$.validator.addMethod("checkPassword", function (value, element) {
    let checkPwd = /^[_0-9a-zA-z]{8,16}$/;
    return this.optional(element) || (checkPwd.test(value));
}, "密码只能包含数字、字母、下划线，长度为8~16位");
$.validator.addMethod("checkCodePassword", function (value, element) {
    let checkPwd = /^[_0-9a-zA-z]{0,16}$/;
    return (checkPwd.test(value));
}, "密码只能包含数字、字母、下划线，最长长度为16位");
$(function () {
    $('#loginModal form').validate({
        rules: {
            email: {
                required: true,
                email: true
            },
            password: {
                required: true,
                checkPassword: true
            }
        }
    });
    $('#regeditModal form').validate({
        rules: {
            email: {
                required: true,
                email: true,
                remote: {
                    url: 'checkEmail',
                    type: 'post'
                }
            },
            password: {
                required: true,
                checkPassword: true
            },
            confirmPassword: {
                checkPassword: true,
                equalTo: '#regPassword'
            }
        },
        messages: {
            email: {
                remote: '该邮箱已被注册，请重新注册'
            }
        }
    });
    $('#changePasswordModal form').validate({
        rules: {
            oldPassword: {
                required: true,
                checkPassword: true,
                remote: {
                    url: 'checkPassword',
                    type: 'post',
                    data: {
                        oldPassword: function (){
                            return CryptoJS.MD5($('#oldPassword').val())
                        }
                    }
                }
            },
            newPassword: {
                required: true,
                checkPassword: true
            },
            confirmPassword: {
                checkPassword: true,
                equalTo: '#newPassword'
            }
        },
        messages: {
            oldPassword: {
                remote: '密码不正确'
            }
        }
    });
    $('#sendModal form').validate({
        rules: {
            times: {
                required: true,
                number: true,
                min: 1,
                max: 9
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