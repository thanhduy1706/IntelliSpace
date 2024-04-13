import type { Metadata } from "next";
import NextTopLoader from "nextjs-toploader";
import Navbar from "@/components/Navbar";
import Footer from "@/components/Footer";

export const metadata: Metadata = {
    title: "Reset | IntelliSpace",
    description: "Reset your password.",
};

const RegisterLayout = ({ children }: { children: React.ReactNode }) => {
    return (
        <>
            <NextTopLoader color="#BF40BF" showSpinner={false} easing="ease" />
            <Navbar />
            {children}
            <Footer />
        </>
    );
};

export default RegisterLayout;
