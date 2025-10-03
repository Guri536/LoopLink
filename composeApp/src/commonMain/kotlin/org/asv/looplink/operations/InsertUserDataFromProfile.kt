package org.asv.looplink.operations

import org.asv.looplink.DatabaseMng
import org.asv.looplink.components.chat.User
import org.asv.looplink.webDriver.studentInfo

fun insertUserDataFromProfile(
    databaseMng: DatabaseMng,
    it: studentInfo,
    myUser: User
){
  databaseMng.insertUserData(
      it.fullName,
      it.uid,
      it.currentSection,
      it.programCode,
      it.studentContact,
      it.cGPA,
      it.studentEmail,
      it.pfpBytes
  )
    myUser.name = it.fullName
    myUser.picture = it.pfpBytes
    // In the future, you may want to load the user's picture here as well.
}