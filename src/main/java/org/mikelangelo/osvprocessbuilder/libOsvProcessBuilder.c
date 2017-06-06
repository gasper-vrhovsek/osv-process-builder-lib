#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "stormy-java/org_mikelangelo_osvprocessbuilder_OsvProcessBuilder.h"

int __attribute__((weak)) osv_execve(const char *path, char *const argv[], char *const envp[], long* thread_id, int notification_fd);
long __attribute__((weak)) osv_waittid(long tid, int *status, int options);

const char* copy_jni_string(JNIEnv *env, jobject obj, jstring j_str) {
  const char *str1, *str2;
  if(j_str == NULL) {
    return NULL;
  }
  str1 = (*env)->GetStringUTFChars(env, j_str, NULL);
  str2 = strdup(str1);
  (*env)->ReleaseStringUTFChars(env, j_str, str1);
  return str2;
}

/*
Input is j_arr - java String[], output is arr - C string array char*[].
Returned arr is NULL terminated (as in C main() argv[]).
Size (number of non-NULL elements, e.g. one less than size of array)
is returned in arr_len if non-NULL.
Returned pointer and pointed-to strings have to be free-ed.
*/
const char** copy_jni_string_arr(JNIEnv *env, jobject obj, jobjectArray j_arr,
  size_t *arr_len) {
  const char** arr;
  jsize arrlen;
  const char* cur_string;
  int ii;

  arrlen = (*env)->GetArrayLength(env, j_arr);
  if(arr_len) {
    *arr_len = arrlen;
  }
  arr = (const char**)malloc(sizeof(char**) * (arrlen+1));
  memset(arr, 0, sizeof(char**) * (arrlen+1)); // in case of strdup error, arr will contain valid pointers and NULLs.
  if(arr==NULL) {
    goto ERROR;
  }
  for (ii=0; ii<arrlen; ii++) {
    jstring j_str = (jstring) (*env)->GetObjectArrayElement(env, j_arr, ii);
    arr[ii] = copy_jni_string(env, obj, j_str);
    if(arr[ii]==NULL) {
      goto ERROR;
    }
  }
  for (ii=0; ii<arrlen; ii++) {
    printf("  arr[%d] = %s\n", ii, arr[ii]);
  }
  return arr;

ERROR:
  if(arr_len) {
    arr_len = 0;
  }
  if(arr) {
    for (ii=0; ii<arrlen; ii++) {
      free((void*)arr[ii]);
      arr[ii] = NULL;
    }
    free(arr);
  }
  return NULL;
}

JNIEXPORT int JNICALL
Java_org_mikelangelo_osvprocessbuilder_OsvProcessBuilder_execve(JNIEnv *env, jobject obj,
    jstring j_path, jobjectArray j_argv, jobjectArray j_envp,
    jlongArray j_thread_id, jint j_notification_fd)
{
  printf("Hello World from JNI!\n");
  printf("  osv_execve = %p\n", osv_execve);

  const char *path;
  path = copy_jni_string(env, obj, j_path);
  printf("  path = %s\n", path);

  size_t argv_len, envp_len;
  char *const* argv;
  char *const* envp;
  argv = (char *const*)copy_jni_string_arr(env, obj, j_argv, &argv_len);
  envp = (char *const*)copy_jni_string_arr(env, obj, j_envp, &envp_len);

  long *p_thread_id = NULL;
  if((*env)->GetArrayLength(env, j_thread_id) > 0) {
    p_thread_id = (*env)->GetLongArrayElements(env, j_thread_id, 0);
  }
  int notification_fd = j_notification_fd;

  int ret = -1;
  if(osv_execve) {
    ret = osv_execve(path, argv, envp, p_thread_id, notification_fd);
    printf("  osv_execve ret=%d\n", ret);
  }
  if(p_thread_id) {
    (*env)->ReleaseLongArrayElements(env, j_thread_id, p_thread_id, 0);
  }
  return ret;
}


JNIEXPORT long JNICALL
Java_org_mikelangelo_osvprocessbuilder_OsvProcessBuilder_waittid(JNIEnv *env, jobject obj,
    jlong j_thread_id, jintArray j_status, jint j_options)
{
  printf("JNI osv_waittid!\n");
  int *p_status = NULL;
  if((*env)->GetArrayLength(env, j_status) > 0) {
    p_status = (*env)->GetIntArrayElements(env, j_status, 0);
  }

  long ret = 0;
  if(osv_waittid) {
    ret = osv_waittid(j_thread_id, p_status, j_options);
  }

  if(p_status) {
    (*env)->ReleaseIntArrayElements(env, j_status, p_status, 0);
  }
  return ret;
}
