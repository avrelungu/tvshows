import {Component} from '@angular/core';
import {LoginFormComponent} from "../login-form/login-form.component";
import {AxiosService} from "../axios.service";
import {CommonModule} from "@angular/common";
import {ButtonsComponent} from "../buttons/buttons.component";
import {WelcomeContentComponent} from "../welcome-content/welcome-content.component";

@Component({
    selector: 'app-content',
    imports: [
        CommonModule,
        LoginFormComponent,
        ButtonsComponent,
        WelcomeContentComponent,
    ],
    templateUrl: './content.component.html',
    standalone: true,
    styleUrls: ['./content.component.css']
})
export class ContentComponent {

    componentToShow: string = "welcome";

    constructor(private axiosService: AxiosService) {
    }

    showComponent(componentToShow: string): void {
        this.componentToShow = componentToShow;
    }

    onLogin($event: any) {
        this.axiosService.request('POST', '/api/auth/login', {
            username: $event.username,
            password: $event.password
        }).then(res => {
            this.axiosService.setAuthToken(res.data.token);
        }).catch(err => {
            console.log(err)
            this.axiosService.setAuthToken(null);
        });
    }

    onRegister($event: any) {
        console.log("trying man");
        this.axiosService.request('POST', '/api/auth/register', {
            username: $event.username,
            firstName: $event.firstName,
            lastName: $event.lastName,
            password: $event.password
        }).then(res => {
            this.axiosService.setAuthToken(res.data.token);
        }).catch(err => {
            console.log(err)
            this.axiosService.setAuthToken(null);
        });
    }
}
