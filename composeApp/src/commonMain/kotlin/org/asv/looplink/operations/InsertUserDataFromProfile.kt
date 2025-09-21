package org.asv.looplink.operations

import org.asv.looplink.DatabaseMng
import org.asv.looplink.webDriver.studentInfo

fun insertUserDataFromProfile(
    databaseMng: DatabaseMng,
    it: studentInfo
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
}