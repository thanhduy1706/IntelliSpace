import type { Metadata } from "next";
import NextTopLoader from "nextjs-toploader";
import Sidebar from "@/components/Sidebar";
import FolderList from "@/components/list/FolderList";

export const metadata: Metadata = {
  title: "Dashboard | IntelliSpace",
  description: "IntelliSpace Dashboard for all your needs",
};

const Dashboardlayout = ({ children }: { children: React.ReactNode }) => {
  return (
    <div className="flex">
      <NextTopLoader color="#BF40BF" showSpinner={false} easing="ease" />
      <Sidebar />
      {children}
    </div>
  );
};

export default Dashboardlayout;
