package controllers

import javax.inject.{Inject, Singleton}
import models.User
import play.api.Logger
import play.api.data.{Form, Forms}
import play.api.i18n._
import play.api.libs.json.{JsError, Json}
import play.api.mvc._

/**
 * Restful Api Controller
 *
 * @author 梦境迷离
 * @version 1.0, 2019-03-15
 */
@Singleton
class RestfulApiController @Inject()(cc: ControllerComponents, langs: Langs, messagesApi: MessagesApi) extends AbstractController(cc) {

    //获取指定的自己定义的打印到控制台的log实例
    val log = Logger("access")

    val lang = langs.availables.head
    //使用errorsAsJson所需要的隐式provider参数
    //注入Lang实例失败，Lang需要一个构造参数，改为注入langs，获取可用的一个
    implicit val messages: Messages = MessagesImpl(lang, messagesApi)

    /**
     * 每个request对象有它自己的Validation 对象来收集错误，这里有三种方法来定义validations;
     *
     * 1.在控制器方法中，直接调用控制器方法来validation 字段，你也可以访问API的play.data.validation.validation类的静态方法的一个子集。
     *
     * 2.添加注释的方法验证控制器的参数声明。
     *
     * 3.添加@Valid注释到action方法的POJO参数，并且给POJO的属性添加校验注释。
     */

    /**
     * 显示登录页面
     *
     * @return
     */
    def login = Action {
        //用的是application
        //Logger.logger.info("用户登录")
        log.info("用户登录")
        Ok(views.html.login("用户登录"))
    }

    /**
     * 模拟用户登录操作
     *
     * 使用元组
     *
     * @return
     */
    def doLogin = Action { implicit request =>
        log.info("表单提交动作")
        val loginForm = Form(
            Forms.tuple(
                "userName" -> Forms.email,
                "password" -> Forms.text(minLength = 6)
            )
        )

        loginForm.bindFromRequest().fold(
            //显示错误信息
            errorForm => Ok(Json.obj("status" -> "ERROR", "errors" -> errorForm.errors.toString())),
            userDate => {
                //获取绑定成功的信息
                val (userName, password) = userDate
                //显示出来
                Ok("userName=" + userName + "&password=" + password)
            }
        )
    }


    /**
     * 模拟用户登录操作
     *
     * 使用case class 只使用id和姓名
     *
     * @return
     */
    def doLogin2 = Action { implicit request =>
        val loginForm = Form(
            Forms.mapping(
                "id" -> Forms.number,
                "userName" -> Forms.text(minLength = 6)
            )(User.apply)(User.unapply)
        )

        loginForm.bindFromRequest().fold(
            //以Json格式写回客户端
            errorForm => Ok(Json.obj("status" -> "ERROR", "errors" -> errorForm.errorsAsJson(messages))),
            userDate => {
                //获取绑定成功的信息
                val user: User = userDate
                //显示出来
                Ok("id=" + user.id + "&userName=" + user.userName)
            }
        )
    }

    /**
     * 无参数
     * /listUser
     *
     * @return
     */
    def listUser() = Action {
        Ok(Json.toJson(User.users))
    }

    /**
     * POST提交，并展示所有User
     * /saveUser
     *
     * @return
     */
    def saveUser = Action(parse.json) { request =>
        val user = request.body.validate[User]
        user.fold(
            errors => {
                BadRequest(Json.obj("status" -> "ERROR", "message" -> JsError.toJson(errors)))
            },
            user => {
                User.addUser(user)
                Ok(Json.obj("status" -> "OK", "users" -> User.users))
            }
        )
    }

    /**
     * 查询参数
     * /getUser?id=1
     *
     * @param id
     * @return
     */
    def getUser(id: Int) = Action {
        User.getUser(id) match {
            case Some(user) => Ok(Json.toJson(user))
            case _ => Ok(Json.obj("status" -> "OK", "message" -> "User Not Found!"))
        }
    }

    /**
     * 路径参数
     * /getUserByPathParam/1
     *
     * @param id
     * @return
     */
    def getUserByPathParam(id: Int) = Action {
        User.getUser(id) match {
            case Some(user) => Ok(Json.toJson(user))
            case _ => Ok(Json.obj("status" -> "OK", "message" -> "User Not Found!"))
        }
    }

    /**
     * 路径参数
     * /deleteUser/1
     *
     * @param id
     * @return
     */
    def deleteUser(id: Int) = Action {
        User.deleteUser(id)
        Ok(Json.obj("status" -> "OK"))
    }

    /**
     * 请求体参数
     * /updateUser
     *
     * @return
     */
    def updateUser = Action(parse.json) { request =>
        val user = request.body.validate[User]
        user.fold(
            errors => {
                BadRequest(Json.obj("status" -> "ERROR", "message" -> JsError.toJson(errors)))
            },
            user => {
                User.updateUser(user)
                Ok(Json.obj("status" -> "OK", "users" -> User.users))
            }
        )
    }
}
