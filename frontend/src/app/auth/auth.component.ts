import { Component } from '@angular/core';
import {AxiosService} from "../axios.service";
import {NgForOf} from "@angular/common";

@Component({
  selector: 'app-auth',
  templateUrl: './auth.component.html',
  imports: [
    NgForOf
  ],
  styleUrl: './auth.component.css'
})
export class AuthComponent {
  data: string[] = [];

  constructor (private axiosService: AxiosService) {
  }
}
