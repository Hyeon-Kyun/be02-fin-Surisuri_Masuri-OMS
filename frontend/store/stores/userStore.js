import { defineStore } from "pinia";
import axios from "axios";

const baseURL = "http://121.140.125.34:11113/api";

export const useUserStore = defineStore({
  id: "user",
  state: () => ({
    userSignUpReq: {
      userName: "",
      userEmail: "",
      userPassword: "",
      confirmPassword: "",
      userPhoneNo: "",
      storeUuid: "",
      storeAddr: "",
      storePhoneNo: "",
    },
    token: null,
  }),
  actions: {
    async createAccount() {
      try {
        const response = await axios.post(
          `${baseURL}/user/register`,
          this.userSignUpReq,
          {
            headers: {
              "Content-Type": "application/json",
            },
          }
        );
        console.log("회원가입 성공", response.data);
        if (response.data.isSuccess) {
          // isSuccess 값이 true인 경우, 회원가입 성공으로 간주
          return true; // 성공적으로 회원가입 완료
        } else {
          
          return response.data.code || "unknown_error";
        }
      } catch (error) {
        console.error("회원가입 실패", error);
        if (error.response && error.response.data && error.response.data.code) {
          // 서버로부터 받은 응답에서 에러 코드가 있을 경우
          return error.response.data.code; // 에러 코드 반환
        }
        return "unknown_error"; // 에러 코드가 없는 경우 기본 에러 코드 반환
      }
    },

    async login(loginReq) {
      try {
        const response = await axios.post(`${baseURL}/user/login`, loginReq, {
          headers: {
            "Content-Type": "application/json",
          },
        });
        const { data } = response;
        if (data.isSuccess) {
          this.token = data.result.jwtToken;
          sessionStorage.setItem("token", this.token);
          console.log("로그인 성공");
          return true; // 로그인 성공
        } else {
          // 서버 응답에 따라 에러 코드 반환
          return data.errorCode; // 예를 들어, 서버 응답에서 제공하는 에러 코드
        }
      } catch (error) {
        if (error.response && error.response.data && error.response.data.code) {
          return error.response.data.code; // 에러 코드 추출
        }
        return "unknown_error"; // 에러 코드가 없는 경우 기본 에러 코드 반환
      }
    },
  },
});
